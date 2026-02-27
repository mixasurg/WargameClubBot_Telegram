package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Запрос на создание или обновление пользователя Telegram.
 *
 * @param telegramId идентификатор пользователя в Telegram
 * @param name отображаемое имя пользователя
 */
public record TelegramUserUpsertRequest(
        @NotNull Long telegramId,
        @NotBlank String name
) {
}
