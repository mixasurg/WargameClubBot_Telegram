package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotBlank;

public record UserRegisterRequest(
        @NotBlank String name
) {
}

