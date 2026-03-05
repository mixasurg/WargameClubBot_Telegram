package com.wargameclub.clubapi.lab.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.wargameclub.clubapi.lab.CircuitState;
import com.wargameclub.clubapi.lab.LabResilienceProperties;
import com.wargameclub.clubapi.lab.exception.CircuitBreakerOpenException;
import com.wargameclub.clubapi.lab.exception.HttpServiceException;
import com.wargameclub.clubapi.lab.exception.NetworkServiceException;
import com.wargameclub.clubapi.lab.exception.PaymentTimeoutException;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

/**
 * Вызывает платежный сервис с Timeout + Retry + Circuit Breaker + Fallback.
 */
@Service
public class PaymentResilienceService {

    public static final String PAYMENT_FALLBACK_MESSAGE = "Оплата не прошла, попробуйте позже";

    private final LabResilienceProperties properties;
    private final PaymentServiceSimulator paymentServiceSimulator;
    private final SimpleCircuitBreaker circuitBreaker;
    private final ExecutorService timeoutExecutor = Executors.newCachedThreadPool();

    public PaymentResilienceService(
            LabResilienceProperties properties,
            PaymentServiceSimulator paymentServiceSimulator
    ) {
        this.properties = properties;
        this.paymentServiceSimulator = paymentServiceSimulator;
        this.circuitBreaker = new SimpleCircuitBreaker(properties.getCircuitBreaker());
    }

    public PaymentExecutionResult processPayment(Long userId, ServiceFaultSettings settings) {
        AttemptCounter attempts = new AttemptCounter();
        try {
            circuitBreaker.execute(() -> {
                executeWithRetry(userId, settings, attempts);
                return null;
            });
            return new PaymentExecutionResult(true, "SUCCESS", "Оплата прошла успешно", attempts.value());
        } catch (CircuitBreakerOpenException ex) {
            return new PaymentExecutionResult(false, "FALLBACK_CIRCUIT_OPEN", PAYMENT_FALLBACK_MESSAGE, attempts.value());
        } catch (Exception ex) {
            return new PaymentExecutionResult(false, "FALLBACK_PAYMENT_ERROR", PAYMENT_FALLBACK_MESSAGE, attempts.value());
        }
    }

    public CircuitState getCircuitState() {
        return circuitBreaker.getState();
    }

    public void reset() {
        circuitBreaker.reset();
    }

    private void executeWithRetry(Long userId, ServiceFaultSettings settings, AttemptCounter attempts) throws Exception {
        int maxAttempts = Math.max(1, properties.getRetry().getMaxAttempts());
        long backoffMs = Math.max(0, properties.getRetry().getInitialDelayMs());
        int multiplier = Math.max(1, properties.getRetry().getMultiplier());

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            attempts.set(attempt);
            try {
                callWithTimeout(userId, settings);
                return;
            } catch (Exception ex) {
                if (!isRetryable(ex) || attempt == maxAttempts) {
                    throw ex;
                }
                sleepBackoff(backoffMs);
                backoffMs = backoffMs * multiplier;
            }
        }
    }

    private void callWithTimeout(Long userId, ServiceFaultSettings settings) throws Exception {
        Future<Void> future = timeoutExecutor.submit(() -> {
            paymentServiceSimulator.processPayment(userId, settings);
            return null;
        });

        try {
            long timeout = Math.max(1, properties.getTimeout().getTimeoutMs());
            future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw new PaymentTimeoutException("Payment call timed out", ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof Exception handled) {
                throw handled;
            }
            throw new IllegalStateException("Unexpected payment failure", cause);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new NetworkServiceException("Interrupted while waiting payment response");
        }
    }

    private boolean isRetryable(Exception exception) {
        if (exception instanceof HttpServiceException httpException) {
            return httpException.getStatus() >= 500;
        }
        return exception instanceof NetworkServiceException;
    }

    private void sleepBackoff(long backoffMs) {
        if (backoffMs <= 0) {
            return;
        }
        try {
            Thread.sleep(backoffMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new NetworkServiceException("Interrupted during retry backoff");
        }
    }

    @PreDestroy
    void shutdown() {
        timeoutExecutor.shutdownNow();
    }

    private static final class AttemptCounter {
        private int value;

        int value() {
            return value;
        }

        void set(int value) {
            this.value = value;
        }
    }
}
