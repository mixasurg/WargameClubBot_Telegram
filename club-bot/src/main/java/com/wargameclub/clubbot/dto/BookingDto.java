package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;

/**
 * Представление бронирования в API бота.
 *
 * @param id идентификатор бронирования
 * @param userId идентификатор автора бронирования
 * @param userName имя автора
 * @param opponentUserId идентификатор соперника
 * @param opponentName имя соперника
 * @param startAt время начала
 * @param endAt время окончания
 * @param game название игры
 * @param tableUnits объем стола
 * @param bookingMode режим бронирования
 * @param joinDeadlineAt дедлайн присоединения
 */
public record BookingDto(
        Long id,
        Long userId,
        String userName,
        Long opponentUserId,
        String opponentName,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String game,
        Integer tableUnits,
        BookingMode bookingMode,
        OffsetDateTime joinDeadlineAt
) {
}
