package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;

public record GameDto(
        Long id,
        String name,
        int defaultDurationMinutes,
        int tableUnits,
        boolean isActive,
        OffsetDateTime createdAt
) {
}

