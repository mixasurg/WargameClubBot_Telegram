package com.wargameclub.clubapi.service;

/**
 * Сервис для работы с сущностью EventPublisher.
 */
public interface EventPublisher {

    /**
     * Публикует EventNotification.
     */
    void publishEventNotification(String messageText);
}

