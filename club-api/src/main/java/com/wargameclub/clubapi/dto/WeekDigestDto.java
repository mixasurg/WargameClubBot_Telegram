package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO для WeekDigest.
 */
public record WeekDigestDto(
        OffsetDateTime weekStart,
        OffsetDateTime weekEnd,
        String timezone,
        List<DigestDayDto> days,
        List<DigestEventDto> events
) {
}

