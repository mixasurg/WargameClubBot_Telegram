package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubapi.enums.BookingStatus;

/**
 * DTO для бронирования.
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
        BookingStatus status,
        OffsetDateTime createdAt
) {
}

