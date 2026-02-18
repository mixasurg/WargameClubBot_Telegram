package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record WeekDigestDto(
        OffsetDateTime weekStart,
        OffsetDateTime weekEnd,
        String timezone,
        List<DigestDayDto> days,
        List<DigestEventDto> events
) {
}

