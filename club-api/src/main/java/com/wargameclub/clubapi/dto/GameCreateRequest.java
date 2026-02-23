package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * DTO запроса на создание игры.
 */
public record GameCreateRequest(
        @NotBlank String name,
        @Positive int defaultDurationMinutes,
        @Positive int tableUnits
) {
}

