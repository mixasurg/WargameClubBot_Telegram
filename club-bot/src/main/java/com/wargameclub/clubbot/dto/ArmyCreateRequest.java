package com.wargameclub.clubbot.dto;

/**
 * Запрос на создание армии.
 *
 * @param ownerUserId идентификатор владельца армии
 * @param game название игры/системы
 * @param faction фракция или подфракция
 * @param isClubShared признак доступности армии для клуба
 */
public record ArmyCreateRequest(
        Long ownerUserId,
        String game,
        String faction,
        boolean isClubShared
) {
}
