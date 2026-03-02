package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Запрос на изменение доступности армии для клуба.
 *
 * @param ownerUserId идентификатор владельца армии
 * @param clubShared новый признак доступности армии для клуба
 */
public record ArmyClubShareUpdateRequest(
        @NotNull Long ownerUserId,
        boolean clubShared
) {
}
