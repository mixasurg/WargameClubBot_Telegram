package com.wargameclub.clubapi.dto;

/**
 * DTO для BookingTableAllocation.
 */
public record BookingTableAllocationDto(
        Long tableId,
        int units
) {
}

