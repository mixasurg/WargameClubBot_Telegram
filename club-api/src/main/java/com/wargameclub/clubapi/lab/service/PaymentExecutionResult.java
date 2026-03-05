package com.wargameclub.clubapi.lab.service;

/**
 * Результат вызова платежного сервиса.
 */
public record PaymentExecutionResult(
        boolean success,
        String status,
        String message,
        int attempts
) {
}
