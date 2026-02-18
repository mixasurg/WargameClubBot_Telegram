package com.wargameclub.clubapi.dto;

import java.util.List;

public record DigestTableBookingsDto(
        Long tableId,
        String tableName,
        List<DigestBookingDto> bookings
) {
}

