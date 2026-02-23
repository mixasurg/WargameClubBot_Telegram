package com.wargameclub.clubapi.dto;

import com.wargameclub.clubapi.enums.GameOutcome;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO запроса на фиксацию результата бронирования.
 */
public record BookingResultRequest(
        @NotNull @Positive Long reporterUserId,
        @NotNull GameOutcome outcome
) {
}
