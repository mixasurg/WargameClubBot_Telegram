package com.wargameclub.clubapi.dto;

import com.wargameclub.clubapi.enums.GameOutcome;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Запрос на фиксацию результата бронирования.
 *
 * @param reporterUserId идентификатор пользователя, сообщившего результат
 * @param outcome исход игры
 */
public record BookingResultRequest(
        @NotNull @Positive Long reporterUserId,
        @NotNull GameOutcome outcome
) {
}
