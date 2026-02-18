package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record GameCreateRequest(
        @NotBlank String name,
        @Positive int defaultDurationMinutes,
        @Positive int tableUnits
) {
}

