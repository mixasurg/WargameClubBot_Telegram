package com.wargameclub.clubapi.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Представление дня в недельном дайджесте.
 *
 * @param date дата дня
 * @param tables список бронирований, сгруппированных по столам
 */
public record DigestDayDto(
        LocalDate date,
        List<DigestTableBookingsDto> tables
) {
}
