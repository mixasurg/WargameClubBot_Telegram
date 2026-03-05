package com.wargameclub.clubapi.lab.exception;

/**
 * Исключение открытого circuit breaker.
 */
public class CircuitBreakerOpenException extends LabServiceException {

    public CircuitBreakerOpenException(String message) {
        super(message);
    }
}
