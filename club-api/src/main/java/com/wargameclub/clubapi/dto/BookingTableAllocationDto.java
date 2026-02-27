package com.wargameclub.clubapi.dto;

/**
 * Представление распределения бронирования по столу.
 *
 * @param tableId идентификатор стола
 * @param units количество занятых единиц стола
 */
public record BookingTableAllocationDto(
        Long tableId,
        int units
) {
}
