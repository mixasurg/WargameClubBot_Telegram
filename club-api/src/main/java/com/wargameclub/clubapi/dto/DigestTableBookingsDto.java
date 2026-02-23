package com.wargameclub.clubapi.dto;

import java.util.List;

/**
 * DTO для DigestTableBookings.
 */
public record DigestTableBookingsDto(
        Long tableId,
        String tableName,
        List<DigestBookingDto> bookings
) {
}

