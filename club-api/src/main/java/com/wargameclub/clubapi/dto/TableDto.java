package com.wargameclub.clubapi.dto;

public record TableDto(
        Long id,
        String name,
        boolean isActive,
        String notes
) {
}

