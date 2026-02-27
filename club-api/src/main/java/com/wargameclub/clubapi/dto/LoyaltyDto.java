package com.wargameclub.clubapi.dto;

/**
 * Представление баланса лояльности пользователя.
 *
 * @param userId идентификатор пользователя
 * @param points количество баллов лояльности
 */
public record LoyaltyDto(
        Long userId,
        int points
) {
}
