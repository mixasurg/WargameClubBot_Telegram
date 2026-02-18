package com.wargameclub.clubbot.service;

public record NotificationMessage(
        Long chatId,
        Integer threadId,
        String text
) {
}

