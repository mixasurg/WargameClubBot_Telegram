package com.wargameclub.clubapi.dto;

/**
 * Представление настроек Telegram.
 *
 * @param chatId идентификатор чата
 * @param scheduleThreadId идентификатор темы расписания
 * @param eventsThreadId идентификатор темы мероприятий
 * @param scheduleTwoweeksMessageId идентификатор сообщения расписания на две недели
 * @param scheduleTwoweeksNextMessageId идентификатор сообщения следующего двухнедельного расписания
 * @param eventsMessageId идентификатор сообщения списка мероприятий
 * @param timezone часовой пояс в строковом виде
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
