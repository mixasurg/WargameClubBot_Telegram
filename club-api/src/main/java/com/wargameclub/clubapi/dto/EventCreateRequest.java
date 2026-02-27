package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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
        @NotBlank String title,
        @NotNull EventType type,
        String description,
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt,
        @NotNull @Positive Long organizerUserId,
        Integer capacity
) {
}
