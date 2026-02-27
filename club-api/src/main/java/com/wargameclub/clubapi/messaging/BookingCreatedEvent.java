package com.wargameclub.clubapi.messaging;

/**
 * Событие создания бронирования.
 *
 * @param bookingId идентификатор бронирования
 */
public record BookingCreatedEvent(
        Long bookingId
) {
}
