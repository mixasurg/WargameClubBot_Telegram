package com.wargameclub.clubbot.service;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Контракт отправки уведомлений в Telegram.
 */
public interface NotificationDispatcher {

    /**
     * Отправляет уведомление.
     *
     * @param message сообщение для отправки
     * @throws TelegramApiException при ошибке отправки в Telegram
     */
    void dispatch(NotificationMessage message) throws TelegramApiException;
}
