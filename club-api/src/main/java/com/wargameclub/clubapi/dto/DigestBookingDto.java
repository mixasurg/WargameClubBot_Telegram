package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.BookingMode;

/**
 * Представление бронирования в дайджесте.
 *
 * @param bookingId идентификатор бронирования
 * @param startAt дата и время начала
 * @param endAt дата и время окончания
 * @param userName имя основного игрока
 * @param opponentName имя соперника
 * @param userFaction фракция основного игрока
 * @param opponentFaction фракция соперника
 * @param game название игры/системы
 * @param tableUnits количество единиц стола
 * @param bookingMode режим бронирования
 */
public record DigestBookingDto(
        Long bookingId,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String userName,
        String opponentName,
        String userFaction,
        String opponentFaction,
        String game,
        Integer tableUnits,
        BookingMode bookingMode
) {
}
