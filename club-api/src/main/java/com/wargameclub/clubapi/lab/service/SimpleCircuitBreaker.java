package com.wargameclub.clubapi.lab.service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import com.wargameclub.clubapi.lab.CircuitState;
import com.wargameclub.clubapi.lab.LabResilienceProperties;
import com.wargameclub.clubapi.lab.exception.CircuitBreakerOpenException;

/**
 * Упрощенная реализация circuit breaker для лабораторного контура.
 */
public class SimpleCircuitBreaker {

    private final int failureRateThreshold;
    private final int slidingWindowSize;
    private final int minimumNumberOfCalls;
    private final long openStateDurationMs;
    private final int halfOpenPermittedCalls;

    private final Deque<Boolean> recentFailures = new ArrayDeque<>();

    private CircuitState state = CircuitState.CLOSED;
    private Instant openUntil = Instant.EPOCH;
    private int halfOpenCalls;
    private int halfOpenSuccesses;

    public SimpleCircuitBreaker(LabResilienceProperties.CircuitBreaker properties) {
        this.failureRateThreshold = properties.getFailureRateThreshold();
        this.slidingWindowSize = properties.getSlidingWindowSize();
        this.minimumNumberOfCalls = properties.getMinimumNumberOfCalls();
        this.openStateDurationMs = properties.getOpenStateDurationMs();
        this.halfOpenPermittedCalls = properties.getHalfOpenPermittedCalls();
    }

    public synchronized <T> T execute(CheckedSupplier<T> supplier) throws Exception {
        beforeCall();
        try {
            T result = supplier.get();
            onSuccess();
            return result;
        } catch (Exception ex) {
            onFailure();
            throw ex;
        }
    }

    public synchronized CircuitState getState() {
        if (state == CircuitState.OPEN && Instant.now().isAfter(openUntil)) {
            moveToHalfOpen();
        }
        return state;
    }

    public synchronized void reset() {
        state = CircuitState.CLOSED;
        recentFailures.clear();
        openUntil = Instant.EPOCH;
        halfOpenCalls = 0;
        halfOpenSuccesses = 0;
    }

    private void beforeCall() {
        if (state == CircuitState.OPEN) {
            if (Instant.now().isBefore(openUntil)) {
                throw new CircuitBreakerOpenException("Payment circuit breaker is OPEN");
            }
            moveToHalfOpen();
        }

        if (state == CircuitState.HALF_OPEN) {
            if (halfOpenCalls >= halfOpenPermittedCalls) {
                throw new CircuitBreakerOpenException("Payment circuit breaker is HALF_OPEN and saturated");
            }
            halfOpenCalls++;
        }
    }

    private void onSuccess() {
        if (state == CircuitState.HALF_OPEN) {
            halfOpenSuccesses++;
            if (halfOpenSuccesses >= halfOpenPermittedCalls) {
                moveToClosed();
            }
            return;
        }

        recordFailure(false);
        evaluateClosedState();
    }

    private void onFailure() {
        if (state == CircuitState.HALF_OPEN) {
            moveToOpen();
            return;
        }

        recordFailure(true);
        evaluateClosedState();
    }

    private void recordFailure(boolean failure) {
        recentFailures.addLast(failure);
        while (recentFailures.size() > slidingWindowSize) {
            recentFailures.removeFirst();
        }
    }

    private void evaluateClosedState() {
        if (state != CircuitState.CLOSED || recentFailures.size() < minimumNumberOfCalls) {
            return;
        }

        int failures = 0;
        for (Boolean failed : recentFailures) {
            if (Boolean.TRUE.equals(failed)) {
                failures++;
            }
        }

        int failureRate = (int) Math.round((failures * 100.0d) / recentFailures.size());
        if (failureRate >= failureRateThreshold) {
            moveToOpen();
        }
    }

    private void moveToOpen() {
        state = CircuitState.OPEN;
        openUntil = Instant.now().plusMillis(openStateDurationMs);
        halfOpenCalls = 0;
        halfOpenSuccesses = 0;
    }

    private void moveToHalfOpen() {
        state = CircuitState.HALF_OPEN;
        halfOpenCalls = 0;
        halfOpenSuccesses = 0;
    }

    private void moveToClosed() {
        state = CircuitState.CLOSED;
        recentFailures.clear();
        halfOpenCalls = 0;
        halfOpenSuccesses = 0;
        openUntil = Instant.EPOCH;
    }

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}
