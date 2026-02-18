package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.EventStatus;
import com.wargameclub.clubapi.enums.EventType;

public record EventUpdateRequest(
        String title,
        EventType type,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        Long organizerUserId,
        Integer capacity,
        EventStatus status
) {
}

