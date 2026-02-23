package com.wargameclub.clubbot.dto;

/**
 * DTO запроса на создание игры.
 */
public record GameCreateRequest(
        String name,
        int defaultDurationMinutes,
        int tableUnits
) {
}

