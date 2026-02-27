package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;

/**
 * Представление статистики игр пользователя.
 *
 * @param userId идентификатор пользователя
 * @param wins количество побед
 * @param losses количество поражений
 * @param draws количество ничьих
 * @param updatedAt дата и время последнего обновления статистики
 */
public record UserGameStatsDto(
        Long userId,
        int wins,
        int losses,
        int draws,
        OffsetDateTime updatedAt
) {
}
