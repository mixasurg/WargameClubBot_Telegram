package com.wargameclub.clubbot.dto;

/**
 * DTO запроса для TelegramUserUpsert.
 */
public record TelegramUserUpsertRequest(
        Long telegramId,
        String name
) {
}

