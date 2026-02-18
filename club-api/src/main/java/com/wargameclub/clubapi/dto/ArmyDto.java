package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;

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

