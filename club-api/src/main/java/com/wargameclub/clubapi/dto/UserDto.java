package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;

public record UserDto(
        Long id,
        String name,
        Long telegramId,
        OffsetDateTime createdAt
) {
}

