package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.EventStatus;
import com.wargameclub.clubapi.enums.EventType;

/**
 * DTO для DigestEvent.
 */
public record DigestEventDto(
        Long id,
        String title,
        EventType type,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String organizerName,
        EventStatus status
) {
}

