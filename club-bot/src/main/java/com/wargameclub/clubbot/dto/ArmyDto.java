package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;

/**
 * DTO для армии.
 */
public record ArmyDto(
        Long id,
        Long ownerUserId,
        String ownerName,
        String game,
        String faction,
        boolean isClubShared,
        boolean isActive,
        OffsetDateTime createdAt
) {
}

