package com.wargameclub.clubapi.lab;

/**
 * Состояния circuit breaker.
 */
public enum CircuitState {
    CLOSED,
    OPEN,
    HALF_OPEN
}
