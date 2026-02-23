package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO запроса для UserRegister.
 */
public record UserRegisterRequest(
        @NotBlank String name
) {
}

