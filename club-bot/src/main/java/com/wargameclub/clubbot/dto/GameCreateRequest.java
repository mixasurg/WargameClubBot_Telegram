package com.wargameclub.clubbot.dto;

/**
 * Запрос на создание игры в каталоге.
 *
 * @param name название игры/системы
 * @param defaultDurationMinutes длительность по умолчанию в минутах
 * @param tableUnits требуемое количество единиц стола
 */
public record GameCreateRequest(
        String name,
        int defaultDurationMinutes,
        int tableUnits
) {
}
