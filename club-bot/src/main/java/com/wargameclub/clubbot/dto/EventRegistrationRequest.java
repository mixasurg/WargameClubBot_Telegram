package com.wargameclub.clubbot.dto;

import java.math.BigDecimal;

/**
 * Запрос на регистрацию/отмену регистрации на мероприятие.
 *
 * @param userId идентификатор пользователя
 * @param count количество мест/билетов (опционально)
 * @param amount сумма оплаты/возврата (опционально)
 */
public record EventRegistrationRequest(
        Long userId,
        Integer count,
        BigDecimal amount
) {
}
