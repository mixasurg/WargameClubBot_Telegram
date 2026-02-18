package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;

public record EventDto(
        Long id,
        String title,
        String type,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        Long organizerUserId,
        String organizerName,
        Integer capacity,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

