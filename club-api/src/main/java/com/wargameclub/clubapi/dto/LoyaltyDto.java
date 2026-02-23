package com.wargameclub.clubapi.dto;

/**
 * DTO для лояльности.
 */
public record LoyaltyDto(
        Long userId,
        int points
) {
}

