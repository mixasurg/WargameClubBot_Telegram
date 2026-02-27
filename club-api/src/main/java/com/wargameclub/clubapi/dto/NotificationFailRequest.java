package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Запрос на фиксацию ошибки отправки уведомления.
 *
 * @param error текст ошибки или причина сбоя
 */
public record NotificationFailRequest(
        @NotBlank String error
) {
}
