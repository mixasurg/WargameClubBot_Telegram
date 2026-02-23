package com.wargameclub.clubbot.service;

/**
 * Сервис для работы с сущностью NotificationMessage.
 */
public record NotificationMessage(
        Long chatId,
        Integer threadId,
        String text
) {
}

