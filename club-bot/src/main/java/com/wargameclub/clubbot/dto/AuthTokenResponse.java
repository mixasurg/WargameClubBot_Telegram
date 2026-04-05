package com.wargameclub.clubbot.dto;

import java.time.OffsetDateTime;

/**
 * Ответ club-api с параметрами JWT-токена.
 *
 * @param accessToken токен доступа
 * @param tokenType тип токена (обычно Bearer)
 * @param expiresAt время истечения токена
 * @param userId идентификатор пользователя
 * @param login логин пользователя
 * @param role роль пользователя
 * @param name отображаемое имя
 */
public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        OffsetDateTime expiresAt,
        Long userId,
        String login,
        String role,
        String name
) {
}
