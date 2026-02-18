package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;

public record EventCreateRequest(
        String title,
        String type,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        Long organizerUserId,
        Integer capacity
) {
}

