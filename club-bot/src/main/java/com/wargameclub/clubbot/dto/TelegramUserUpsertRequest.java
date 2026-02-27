package com.wargameclub.clubbot.dto;

/**
 * Запрос на создание или обновление пользователя Telegram.
 *
 * @param telegramId идентификатор пользователя в Telegram
 * @param name отображаемое имя пользователя
 */
public record TelegramUserUpsertRequest(
        Long telegramId,
        String name
) {
}
