package com.wargameclub.clubapi.lab.dto;

import com.wargameclub.clubapi.lab.CircuitState;

/**
 * Диагностическая статистика лабораторного контура.
 */
public record LabResilienceStatsDto(
        CircuitState paymentCircuitState,
        int paymentServiceCalls,
        int notificationServiceCalls,
        int queuedNotifications
) {
}
