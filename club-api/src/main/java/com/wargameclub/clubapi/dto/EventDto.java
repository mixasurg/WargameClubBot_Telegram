package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.EventStatus;
import com.wargameclub.clubapi.enums.EventType;

/**
 * Представление мероприятия для API.
 *
 * @param id идентификатор мероприятия
 * @param title название мероприятия
 * @param type тип мероприятия
 * @param description описание мероприятия
 * @param startAt дата и время начала
 * @param endAt дата и время окончания
 * @param organizerUserId идентификатор пользователя-организатора
 * @param organizerName имя организатора
 * @param capacity максимальное число участников
 * @param status статус мероприятия
 * @param createdAt дата и время создания
 * @param updatedAt дата и время последнего обновления
 */
public record EventDto(
        Long id,
        String title,
        EventType type,
        String description,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        Long organizerUserId,
        String organizerName,
        Integer capacity,
        EventStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
