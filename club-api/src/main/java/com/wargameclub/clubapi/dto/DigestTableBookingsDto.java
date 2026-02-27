package com.wargameclub.clubapi.dto;

import java.util.List;

/**
 * Представление бронирований по конкретному столу в дайджесте.
 *
 * @param tableId идентификатор стола
 * @param tableName название стола
 * @param bookings список бронирований стола
 */
public record DigestTableBookingsDto(
        Long tableId,
        String tableName,
        List<DigestBookingDto> bookings
) {
}
