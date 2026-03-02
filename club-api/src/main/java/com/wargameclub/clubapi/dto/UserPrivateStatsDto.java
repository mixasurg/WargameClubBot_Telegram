package com.wargameclub.clubapi.dto;

/**
 * Сводная статистика пользователя для личного меню.
 *
 * @param userId идентификатор пользователя
 * @param loyaltyPoints очки лояльности
 * @param totalGames общее количество сыгранных игр
 * @param gamesLastMonth количество игр за последние 30 дней
 * @param winRateTotal процент побед за все время
 * @param winRateLastMonth процент побед за последние 30 дней
 */
public record UserPrivateStatsDto(
        Long userId,
        int loyaltyPoints,
        int totalGames,
        int gamesLastMonth,
        double winRateTotal,
        double winRateLastMonth
) {
}
