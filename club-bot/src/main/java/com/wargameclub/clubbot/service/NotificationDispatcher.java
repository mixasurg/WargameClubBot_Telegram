package com.wargameclub.clubbot.service;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface NotificationDispatcher {
    void dispatch(NotificationMessage message) throws TelegramApiException;
}

