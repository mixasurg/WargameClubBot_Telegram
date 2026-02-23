package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO запроса для TelegramUserUpsert.
 */
public record TelegramUserUpsertRequest(
        @NotNull Long telegramId,
        @NotBlank String name
) {
}

