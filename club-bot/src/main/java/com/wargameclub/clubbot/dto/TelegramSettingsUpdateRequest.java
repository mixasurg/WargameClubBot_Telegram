package com.wargameclub.clubbot.dto;

/**
 * Запрос на обновление настроек Telegram.
 *
 * @param chatId идентификатор чата
 * @param scheduleThreadId идентификатор темы расписания
 * @param eventsThreadId идентификатор темы мероприятий
 * @param scheduleTwoweeksMessageId идентификатор сообщения расписания на две недели
 * @param scheduleTwoweeksNextMessageId идентификатор следующего двухнедельного расписания
 * @param eventsMessageId идентификатор сообщения списка мероприятий
 * @param timezone часовой пояс
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
