package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;

/**
 * DTO для пользователя.
 */
public record UserDto(
        Long id,
        String name,
        Long telegramId,
        OffsetDateTime createdAt
) {
}

