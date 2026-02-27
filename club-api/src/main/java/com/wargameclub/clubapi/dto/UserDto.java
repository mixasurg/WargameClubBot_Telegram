package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;

/**
 * Представление пользователя для API.
 *
 * @param id идентификатор пользователя
 * @param name имя пользователя
 * @param telegramId идентификатор пользователя в Telegram
 * @param createdAt дата и время регистрации
 */
public record UserDto(
        Long id,
        String name,
        Long telegramId,
        OffsetDateTime createdAt
) {
}
