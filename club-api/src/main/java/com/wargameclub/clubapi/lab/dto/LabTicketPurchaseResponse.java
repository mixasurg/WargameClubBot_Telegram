package com.wargameclub.clubapi.lab.dto;

import com.wargameclub.clubapi.lab.CircuitState;

/**
 * Ответ лабораторного сценария покупки билета.
 */
public record LabTicketPurchaseResponse(
        boolean ticketBooked,
        String message,
        String paymentStatus,
        String notificationStatus,
        int paymentAttempts,
        CircuitState paymentCircuitState,
        int paymentServiceCalls,
        int notificationServiceCalls,
        int queuedNotifications
) {
}
