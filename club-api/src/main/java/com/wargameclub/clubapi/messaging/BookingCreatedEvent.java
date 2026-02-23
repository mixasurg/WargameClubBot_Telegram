package com.wargameclub.clubapi.messaging;

/**
 * Событие для BookingCreated.
 */
public record BookingCreatedEvent(
        Long bookingId
) {
}
