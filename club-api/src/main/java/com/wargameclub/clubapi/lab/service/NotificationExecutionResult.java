package com.wargameclub.clubapi.lab.service;

/**
 * Результат вызова сервиса уведомлений.
 */
public record NotificationExecutionResult(
        boolean delivered,
        String status
) {
}
