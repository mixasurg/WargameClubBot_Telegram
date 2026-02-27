package com.wargameclub.clubbot.dto;

/**
 * Запрос на фиксацию результата бронирования.
 *
 * @param reporterUserId идентификатор пользователя, сообщившего результат
 * @param outcome исход игры (строковое значение)
 */
public record BookingResultRequest(
        Long reporterUserId,
        String outcome
) {
}
