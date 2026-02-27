package com.wargameclub.clubbot.dto;

import java.util.UUID;

/**
 * Представление уведомления из outbox-очереди.
 *
 * @param id идентификатор уведомления
 * @param chatRouting JSON-маршрут доставки уведомления
 * @param text текст уведомления
 */
public record NotificationOutboxDto(
        UUID id,
        String chatRouting,
        String text
) {
}
