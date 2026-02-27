package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.EventStatus;
import com.wargameclub.clubapi.enums.EventType;

/**
 * Запрос на обновление мероприятия (частичное обновление).
 *
 * @param title новое название (опционально)
 * @param type новый тип мероприятия (опционально)
 * @param description новое описание (опционально)
 * @param startAt новая дата и время начала (опционально)
 * @param endAt новая дата и время окончания (опционально)
 * @param organizerUserId новый идентификатор организатора (опционально)
 * @param capacity новое ограничение по числу участников (опционально)
 * @param status новый статус мероприятия (опционально)
 */
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
