package com.wargameclub.clubapi.dto;

/**
 * DTO для настроек Telegram.
 */
public record TelegramSettingsDto(
        Long chatId,
        Integer scheduleThreadId,
        Integer eventsThreadId,
        Integer scheduleTwoweeksMessageId,
        Integer scheduleTwoweeksNextMessageId,
        Integer eventsMessageId,
        String timezone
) {
}

