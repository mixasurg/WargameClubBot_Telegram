package com.wargameclub.clubbot.dto;

/**
 * Запрос на фиксацию ошибки отправки уведомления.
 *
 * @param error текст ошибки или причина сбоя
 */
public record NotificationFailRequest(
        String error
) {
}
