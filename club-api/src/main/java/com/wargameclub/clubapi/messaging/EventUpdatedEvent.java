package com.wargameclub.clubapi.messaging;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.EventStatus;
import com.wargameclub.clubapi.enums.EventType;

/**
 * Событие обновления мероприятия.
 *
 * @param eventId идентификатор мероприятия
 * @param title название мероприятия
 * @param type тип мероприятия
 * @param status статус мероприятия
 * @param startAt время начала
 * @param endAt время окончания
 * @param updatedAt время обновления
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
