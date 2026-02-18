package com.wargameclub.clubbot.dto;

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

