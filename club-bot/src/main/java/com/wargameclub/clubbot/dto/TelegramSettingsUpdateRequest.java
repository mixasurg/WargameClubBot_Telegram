package com.wargameclub.clubbot.dto;

/**
 * DTO запроса на обновление настроек Telegram.
 */
public record TelegramSettingsUpdateRequest(
        Long chatId,
        Integer scheduleThreadId,
        Integer eventsThreadId,
        Integer scheduleTwoweeksMessageId,
        Integer scheduleTwoweeksNextMessageId,
        Integer eventsMessageId,
        String timezone
) {
}

