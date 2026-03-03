package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;

/**
 * Запрос на присоединение к открытой игре.
 *
 * @param userId идентификатор присоединяющегося пользователя
 * @param armyId идентификатор армии (опционально)
 * @param faction фракция соперника (опционально)
 */
public record BookingJoinRequest(
        @NotNull @Positive Long userId,
        @Positive Long armyId,
        String faction
) {
}
