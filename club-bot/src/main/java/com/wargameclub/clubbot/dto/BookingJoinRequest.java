package com.wargameclub.clubbot.dto;

/**
 * Запрос на присоединение к открытому бронированию.
 *
 * @param userId идентификатор пользователя
 * @param armyId идентификатор армии (опционально)
 * @param faction фракция (опционально)
 */
public record BookingJoinRequest(
        Long userId,
        Long armyId,
        String faction
) {
}
