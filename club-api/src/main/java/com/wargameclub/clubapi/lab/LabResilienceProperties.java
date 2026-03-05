package com.wargameclub.clubapi.lab;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурация лабораторного контура отказоустойчивости.
 */
@ConfigurationProperties(prefix = "lab.resilience")
public class LabResilienceProperties {

    private final ServiceSimulation payment = new ServiceSimulation();
    private final ServiceSimulation notification = new ServiceSimulation();
    private final PaymentTimeout timeout = new PaymentTimeout();
    private final Retry retry = new Retry();
    private final CircuitBreaker circuitBreaker = new CircuitBreaker();
    private final Bulkhead bulkhead = new Bulkhead();
    private final RateLimiter rateLimiter = new RateLimiter();

    public ServiceSimulation getPayment() {
        return payment;
    }

    public ServiceSimulation getNotification() {
        return notification;
    }

    public PaymentTimeout getTimeout() {
        return timeout;
    }

    public Retry getRetry() {
        return retry;
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    public Bulkhead getBulkhead() {
        return bulkhead;
    }

    public RateLimiter getRateLimiter() {
        return rateLimiter;
    }

    public static class ServiceSimulation {
        private FaultMode mode = FaultMode.NORMAL;
        private long slowDelayMs = 5000;
        private double errorProbability = 0.5;

        public FaultMode getMode() {
            return mode;
        }

        public void setMode(FaultMode mode) {
            this.mode = mode;
        }

        public long getSlowDelayMs() {
            return slowDelayMs;
        }

        public void setSlowDelayMs(long slowDelayMs) {
            this.slowDelayMs = slowDelayMs;
        }

        public double getErrorProbability() {
            return errorProbability;
        }

        public void setErrorProbability(double errorProbability) {
            this.errorProbability = errorProbability;
        }
    }

    public static class PaymentTimeout {
        private long timeoutMs = 2000;

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }

    public static class Retry {
        private int maxAttempts = 3;
        private long initialDelayMs = 100;
        private int multiplier = 2;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialDelayMs() {
            return initialDelayMs;
        }

        public void setInitialDelayMs(long initialDelayMs) {
            this.initialDelayMs = initialDelayMs;
        }

        public int getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(int multiplier) {
            this.multiplier = multiplier;
        }
    }

    public static class CircuitBreaker {
        private int failureRateThreshold = 50;
        private int slidingWindowSize = 10;
        private int minimumNumberOfCalls = 5;
        private long openStateDurationMs = 10000;
        private int halfOpenPermittedCalls = 5;

        public int getFailureRateThreshold() {
            return failureRateThreshold;
        }

        public void setFailureRateThreshold(int failureRateThreshold) {
            this.failureRateThreshold = failureRateThreshold;
        }

        public int getSlidingWindowSize() {
            return slidingWindowSize;
        }

        public void setSlidingWindowSize(int slidingWindowSize) {
            this.slidingWindowSize = slidingWindowSize;
        }

        public int getMinimumNumberOfCalls() {
            return minimumNumberOfCalls;
        }

        public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
            this.minimumNumberOfCalls = minimumNumberOfCalls;
        }

        public long getOpenStateDurationMs() {
            return openStateDurationMs;
        }

        public void setOpenStateDurationMs(long openStateDurationMs) {
            this.openStateDurationMs = openStateDurationMs;
        }

        public int getHalfOpenPermittedCalls() {
            return halfOpenPermittedCalls;
        }

        public void setHalfOpenPermittedCalls(int halfOpenPermittedCalls) {
            this.halfOpenPermittedCalls = halfOpenPermittedCalls;
        }
    }

    public static class Bulkhead {
        private int maxConcurrentCalls = 2;
        private int queueCapacity = 0;

        public int getMaxConcurrentCalls() {
            return maxConcurrentCalls;
        }

        public void setMaxConcurrentCalls(int maxConcurrentCalls) {
            this.maxConcurrentCalls = maxConcurrentCalls;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }
    }

    public static class RateLimiter {
        private int maxRequestsPerMinute = 5;
        private long windowMs = 60000;

        public int getMaxRequestsPerMinute() {
            return maxRequestsPerMinute;
        }

        public void setMaxRequestsPerMinute(int maxRequestsPerMinute) {
            this.maxRequestsPerMinute = maxRequestsPerMinute;
        }

        public long getWindowMs() {
            return windowMs;
        }

        public void setWindowMs(long windowMs) {
            this.windowMs = windowMs;
        }
    }
}
