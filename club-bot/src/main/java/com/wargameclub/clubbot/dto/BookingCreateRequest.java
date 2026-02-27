package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;

/**
 * Запрос на создание бронирования.
 *
 * @param tableId идентификатор стола (опционально)
 * @param userId идентификатор пользователя
 * @param startAt дата и время начала
 * @param endAt дата и время окончания
 * @param game название игры/системы
 * @param tableUnits требуемое количество единиц стола
 * @param opponentUserId идентификатор соперника (опционально)
 * @param armyId идентификатор армии (опционально)
 * @param notes дополнительные примечания (опционально)
 */
public record BookingCreateRequest(
        Long tableId,
        Long userId,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String game,
        Integer tableUnits,
        Long opponentUserId,
        Long armyId,
        String notes
) {
}
