package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ArmyUsageRequest(
        @NotNull @Positive Long usedByUserId,
        @NotNull OffsetDateTime usedAt,
        String notes
) {
}

