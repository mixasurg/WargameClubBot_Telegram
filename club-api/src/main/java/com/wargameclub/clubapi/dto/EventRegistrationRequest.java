package com.wargameclub.clubapi.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EventRegistrationRequest(
        @NotNull @Positive Long userId,
        @Positive Integer count,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal amount
) {
}

