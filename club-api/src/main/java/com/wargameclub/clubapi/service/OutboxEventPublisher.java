package com.wargameclub.clubapi.service;

import com.wargameclub.clubapi.enums.NotificationTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с сущностью OutboxEventPublisher.
 */
@Service
public class OutboxEventPublisher implements EventPublisher {
    /**
     * Логгер.
     */
    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);

    /**
     * Сервис NotificationOutbox.
     */
    private final NotificationOutboxService outboxService;

    /**
     * Сервис настроек Telegram.
     */
    private final TelegramSettingsService settingsService;

    /**
     * Конструктор OutboxEventPublisher.
     */
    public OutboxEventPublisher(NotificationOutboxService outboxService, TelegramSettingsService settingsService) {
        this.outboxService = outboxService;
        this.settingsService = settingsService;
    }

    /**
     * Публикует EventNotification.
     */
    @Override
    public void publishEventNotification(String messageText) {
        settingsService.getAny().ifPresentOrElse(settings -> {
            ChatRouting routing = new ChatRouting(settings.getChatId(), settings.getScheduleThreadId());
            outboxService.enqueue(NotificationTarget.TELEGRAM, routing, messageText);
        }, () -> log.warn("Пропуск уведомления: настройки Telegram не заданы"));
    }
}

