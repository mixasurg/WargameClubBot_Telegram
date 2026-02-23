package com.wargameclub.clubbot.service;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Сервис для работы с сущностью NotificationDispatcher.
 */
public interface NotificationDispatcher {

    /**
     * Выполняет операцию.
     */
    void dispatch(NotificationMessage message) throws TelegramApiException;
}

