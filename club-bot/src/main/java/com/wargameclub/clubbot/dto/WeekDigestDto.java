package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Представление недельного дайджеста.
 *
 * @param weekStart начало недели
 * @param weekEnd конец недели
 * @param timezone часовой пояс, в котором сформирован дайджест
 * @param days список дней с бронированиями
 * @param events список мероприятий недели
 */
public record WeekDigestDto(
        OffsetDateTime weekStart,
        OffsetDateTime weekEnd,
        String timezone,
        List<DigestDayDto> days,
        List<DigestEventDto> events
) {
}
