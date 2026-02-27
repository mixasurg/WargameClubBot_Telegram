package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;

/**
 * Представление армии для API.
 *
 * @param id идентификатор армии
 * @param ownerUserId идентификатор владельца
 * @param ownerName имя владельца
 * @param game название игры/системы
 * @param faction фракция или подфракция
 * @param isClubShared признак клубной армии
 * @param isActive признак активности армии
 * @param createdAt дата и время создания
 */
public record ArmyDto(
        Long id,
        Long ownerUserId,
        String ownerName,
        String game,
        String faction,
        boolean isClubShared,
        boolean isActive,
        OffsetDateTime createdAt
) {
}
