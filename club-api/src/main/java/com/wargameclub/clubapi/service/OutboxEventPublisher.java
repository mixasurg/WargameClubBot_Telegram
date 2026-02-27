package com.wargameclub.clubapi.service;

import com.wargameclub.clubapi.enums.NotificationTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Публикатор уведомлений, отправляющий сообщения через outbox-очередь.
 */
@Service
public class OutboxEventPublisher implements EventPublisher {
    /**
     * Логгер.
     */
    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);

    /**
     * Сервис outbox-уведомлений.
     */
    private final NotificationOutboxService outboxService;

    /**
     * Сервис настроек Telegram.
     */
    private final TelegramSettingsService settingsService;

    /**
     * Создает публикатор уведомлений через outbox.
     *
     * @param outboxService сервис outbox
     * @param settingsService сервис настроек Telegram
     */
    public OutboxEventPublisher(NotificationOutboxService outboxService, TelegramSettingsService settingsService) {
        this.outboxService = outboxService;
        this.settingsService = settingsService;
    }

    /**
     * Публикует уведомление о событии, если заданы настройки Telegram.
     *
     * @param messageText текст уведомления
     */
    @Override
    public void publishEventNotification(String messageText) {
        settingsService.getAny().ifPresentOrElse(settings -> {
            ChatRouting routing = new ChatRouting(settings.getChatId(), settings.getScheduleThreadId());
            outboxService.enqueue(NotificationTarget.TELEGRAM, routing, messageText);
        }, () -> log.warn("Пропуск уведомления: настройки Telegram не заданы"));
    }
}
