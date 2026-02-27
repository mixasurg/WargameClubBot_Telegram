package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Запрос на создание бронирования.
 *
 * @param tableId идентификатор стола (опционально, если назначение стола выполняется позже)
 * @param userId идентификатор пользователя, создающего бронирование
 * @param startAt дата и время начала
 * @param endAt дата и время окончания
 * @param game название игры/системы
 * @param tableUnits требуемое количество единиц стола
 * @param opponentUserId идентификатор соперника (опционально)
 * @param armyId идентификатор выбранной армии (опционально)
 * @param notes дополнительные примечания (опционально)
 */
public record BookingCreateRequest(
        @Positive Long tableId,
        @NotNull @Positive Long userId,
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt,
        @NotBlank String game,
        @NotNull @Min(1) @Max(6) Integer tableUnits,
        @Positive Long opponentUserId,
        @Positive Long armyId,
        String notes
) {
}
