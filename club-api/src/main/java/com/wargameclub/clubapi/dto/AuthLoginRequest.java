package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос входа пользователя в систему.
 *
 * @param login логин пользователя
 * @param password пароль пользователя
 */
public record AuthLoginRequest(
        @NotBlank @Size(min = 3, max = 120) String login,
        @NotBlank @Size(min = 6, max = 120) String password
) {
}
