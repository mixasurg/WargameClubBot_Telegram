package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;

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
        String type,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String organizerName,
        String status
) {
}
