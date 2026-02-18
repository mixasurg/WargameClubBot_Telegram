package com.wargameclub.clubbot.dto;

public record TelegramUserUpsertRequest(
        Long telegramId,
        String name
) {
}

