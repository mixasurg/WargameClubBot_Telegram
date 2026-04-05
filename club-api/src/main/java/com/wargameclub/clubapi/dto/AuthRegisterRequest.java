package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос регистрации пользователя для JWT-аутентификации.
 *
 * @param name отображаемое имя пользователя
 * @param login логин пользователя
 * @param password пароль пользователя
 */
public record AuthRegisterRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(min = 3, max = 120) String login,
        @NotBlank @Size(min = 6, max = 120) String password
) {
}
