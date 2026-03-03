package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubapi.enums.BookingMode;
import com.wargameclub.clubapi.enums.BookingStatus;

/**
 * Представление бронирования для API.
 *
 * @param id идентификатор бронирования
 * @param tableId идентификатор основного стола
 * @param tableName название стола
 * @param tableAssignments распределение бронирования по столам и единицам
 * @param userId идентификатор пользователя, создавшего бронирование
 * @param userName имя пользователя
 * @param startAt дата и время начала
 * @param endAt дата и время окончания
 * @param game название игры/системы
 * @param tableUnits количество единиц стола
 * @param opponentUserId идентификатор соперника
 * @param opponentName имя соперника
 * @param armyId идентификатор выбранной армии
 * @param armyName название армии
 * @param notes дополнительные примечания
 * @param bookingMode режим бронирования
 * @param joinDeadlineAt дедлайн присоединения к открытой игре
 * @param status статус бронирования
 * @param cancelReason причина отмены
 * @param createdAt дата и время создания
 */
public record BookingDto(
        Long id,
        Long tableId,
        String tableName,
        List<BookingTableAllocationDto> tableAssignments,
        Long userId,
        String userName,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String game,
        Integer tableUnits,
        Long opponentUserId,
        String opponentName,
        Long armyId,
        String armyName,
        String notes,
        BookingMode bookingMode,
        OffsetDateTime joinDeadlineAt,
        BookingStatus status,
        String cancelReason,
        OffsetDateTime createdAt
) {
}
