package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO запроса на создание бронирования.
 */
public record BookingCreateRequest(
        @Positive Long tableId,
        @NotNull @Positive Long userId,
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt,
        @NotBlank String game,
        @NotNull @Min(1) @Max(6) Integer tableUnits,
        @Positive Long opponentUserId,
        @Positive Long armyId,
        String notes
) {
}

