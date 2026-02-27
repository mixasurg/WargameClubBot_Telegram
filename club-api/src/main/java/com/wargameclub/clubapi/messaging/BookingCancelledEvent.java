package com.wargameclub.clubapi.messaging;

/**
 * Событие отмены бронирования.
 *
 * @param bookingId идентификатор бронирования
 */
public record BookingCancelledEvent(
        Long bookingId
) {
}
