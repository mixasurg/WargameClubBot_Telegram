package com.wargameclub.clubapi.dto;

/**
 * Ответ эндпоинта текущего пользователя.
 *
 * @param userId идентификатор пользователя
 * @param login логин пользователя
 * @param role роль пользователя
 * @param name имя пользователя
 */
public record AuthMeResponse(
        Long userId,
        String login,
        String role,
        String name
) {
}
