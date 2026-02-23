package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;

/**
 * DTO для статистики игрока.
 */
public record UserGameStatsDto(
        Long userId,
        int wins,
        int losses,
        int draws,
        OffsetDateTime updatedAt
) {
}
