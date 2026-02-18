package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;

public record DigestEventDto(
        Long id,
        String title,
        String type,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String organizerName,
        String status
) {
}

