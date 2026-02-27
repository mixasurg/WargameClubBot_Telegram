package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Запрос на создание армии.
 *
 * @param ownerUserId идентификатор владельца армии
 * @param game название игры/системы
 * @param faction фракция или подфракция
 * @param isClubShared признак доступности армии для клуба
 */
public record ArmyCreateRequest(
        @NotNull @Positive Long ownerUserId,
        @NotBlank String game,
        @NotBlank String faction,
        boolean isClubShared
) {
}
