package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotNull;

public record TelegramSettingsUpdateRequest(
        @NotNull Long chatId,
        Integer scheduleThreadId,
        Integer eventsThreadId,
        Integer scheduleTwoweeksMessageId,
        Integer scheduleTwoweeksNextMessageId,
        Integer eventsMessageId,
        String timezone
) {
}

