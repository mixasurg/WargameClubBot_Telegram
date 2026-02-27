package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;

/**
 * Представление бронирования в дайджесте.
 *
 * @param startAt дата и время начала
 * @param endAt дата и время окончания
 * @param userName имя основного игрока
 * @param opponentName имя соперника
 * @param userFaction фракция основного игрока
 * @param opponentFaction фракция соперника
 * @param game название игры/системы
 * @param tableUnits количество единиц стола
 */
public record DigestBookingDto(
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String userName,
        String opponentName,
        String userFaction,
        String opponentFaction,
        String game,
        Integer tableUnits
) {
}
