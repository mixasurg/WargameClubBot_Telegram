package com.wargameclub.clubapi.service;

/**
 * Маршрут доставки сообщения в Telegram.
 *
 * @param chatId идентификатор чата
 * @param threadId идентификатор темы/треда (опционально)
 */
public record ChatRouting(
        Long chatId,
        Integer threadId
) {
}
