package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;

/**
 * DTO для игры.
 */
public record GameDto(
        Long id,
        String name,
        int defaultDurationMinutes,
        int tableUnits,
        boolean isActive,
        OffsetDateTime createdAt
) {
}

