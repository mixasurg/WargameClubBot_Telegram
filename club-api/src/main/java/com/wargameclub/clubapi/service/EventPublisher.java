package com.wargameclub.clubapi.service;

/**
 * Контракт публикации уведомлений о событиях клуба.
 */
public interface EventPublisher {

    /**
     * Публикует уведомление о событии.
     *
     * @param messageText текст уведомления
     */
    void publishEventNotification(String messageText);
}
