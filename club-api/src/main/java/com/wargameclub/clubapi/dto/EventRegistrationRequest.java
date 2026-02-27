package com.wargameclub.clubapi.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Запрос на регистрацию или отмену регистрации на мероприятие.
 *
 * @param userId идентификатор пользователя
 * @param count количество мест/участников (опционально)
 * @param amount сумма оплаты (опционально)
 */
public record EventRegistrationRequest(
        @NotNull @Positive Long userId,
        @Positive Integer count,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal amount
) {
}
