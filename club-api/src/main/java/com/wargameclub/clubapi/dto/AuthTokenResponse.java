package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;

/**
 * Ответ с параметрами JWT-токена.
 *
 * @param accessToken JWT access token
 * @param tokenType тип токена (Bearer)
 * @param expiresAt время истечения токена
 * @param userId идентификатор пользователя
 * @param login логин пользователя
 * @param role роль пользователя
 * @param name отображаемое имя пользователя
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
