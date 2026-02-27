package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.EventStatus;
import com.wargameclub.clubapi.enums.EventType;

/**
 * Представление мероприятия в дайджесте.
 *
 * @param id идентификатор мероприятия
 * @param title название мероприятия
 * @param type тип мероприятия
 * @param startAt дата и время начала
 * @param endAt дата и время окончания
 * @param organizerName имя организатора
 * @param status статус мероприятия
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
