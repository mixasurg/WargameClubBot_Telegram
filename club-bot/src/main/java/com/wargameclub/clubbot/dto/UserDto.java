package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;

public record UserDto(
        Long id,
        String name,
        Long telegramId,
        OffsetDateTime createdAt
) {
}

