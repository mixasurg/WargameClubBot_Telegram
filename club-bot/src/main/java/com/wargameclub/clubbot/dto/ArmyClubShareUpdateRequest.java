package com.wargameclub.clubbot.dto;

/**
 * Запрос на изменение доступности армии для клуба.
 *
 * @param ownerUserId идентификатор владельца армии
 * @param clubShared новый признак доступности армии для клуба
 */
public record ArmyClubShareUpdateRequest(
        Long ownerUserId,
        boolean clubShared
) {
}
