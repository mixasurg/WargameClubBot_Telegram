package com.wargameclub.clubbot.dto;

import java.util.List;

public record DigestTableBookingsDto(
        Long tableId,
        String tableName,
        List<DigestBookingDto> bookings
) {
}

