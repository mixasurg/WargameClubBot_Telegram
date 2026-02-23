package com.wargameclub.clubapi.messaging;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.EventStatus;
import com.wargameclub.clubapi.enums.EventType;

/**
 * Событие для EventUpdated.
 */
public record EventUpdatedEvent(
        Long eventId,
        String title,
        EventType type,
        EventStatus status,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        OffsetDateTime updatedAt
) {
}
