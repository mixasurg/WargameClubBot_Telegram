package com.wargameclub.clubbot.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO для DigestDay.
 */
public record DigestDayDto(
        LocalDate date,
        List<DigestTableBookingsDto> tables
) {
}

