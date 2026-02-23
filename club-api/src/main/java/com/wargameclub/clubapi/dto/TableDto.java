package com.wargameclub.clubapi.dto;

/**
 * DTO для стола.
 */
public record TableDto(
        Long id,
        String name,
        boolean isActive,
        String notes
) {
}

