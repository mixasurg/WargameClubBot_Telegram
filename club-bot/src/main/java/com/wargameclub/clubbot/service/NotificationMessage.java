package com.wargameclub.clubbot.service;

/**
 * Сообщение для отправки в Telegram.
 *
 * @param chatId идентификатор чата
 * @param threadId идентификатор темы/треда (опционально)
 * @param text текст сообщения
 */
public record NotificationMessage(
        Long chatId,
        Integer threadId,
        String text
) {
}
