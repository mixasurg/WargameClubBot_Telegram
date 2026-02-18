package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;

public record DigestBookingDto(
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String userName,
        String opponentName,
        String userFaction,
        String opponentFaction,
        String game,
        Integer tableUnits
) {
}

