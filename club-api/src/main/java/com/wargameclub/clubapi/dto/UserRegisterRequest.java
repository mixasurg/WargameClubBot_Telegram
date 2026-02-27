package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Запрос на регистрацию пользователя.
 *
 * @param name имя пользователя
 */
public record UserRegisterRequest(
        @NotBlank String name
) {
}
