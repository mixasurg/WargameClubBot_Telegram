package com.wargameclub.clubapi.messaging;

/**
 * Событие для BookingCancelled.
 */
public record BookingCancelledEvent(
        Long bookingId
) {
}
