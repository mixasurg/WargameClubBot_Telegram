package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;

/**
 * Запрос на создание мероприятия.
 *
 * @param title название мероприятия
 * @param type тип мероприятия
 * @param description описание мероприятия
 * @param startAt дата и время начала
 * @param endAt дата и время окончания
 * @param organizerUserId идентификатор пользователя-организатора
 * @param capacity максимальное число участников (опционально)
 */
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
