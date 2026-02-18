package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;

public record BookingCreateRequest(
        Long tableId,
        Long userId,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String game,
        Integer tableUnits,
        Long opponentUserId,
        Long armyId,
        String notes
) {
}

