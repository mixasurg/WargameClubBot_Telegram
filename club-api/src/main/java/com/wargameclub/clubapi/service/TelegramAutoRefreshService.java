package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.NotificationTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с сущностью TelegramAutoRefresh.
 */
@Service
public class TelegramAutoRefreshService {
    /**
     * Логгер.
     */
    private static final Logger log = LoggerFactory.getLogger(TelegramAutoRefreshService.class);

    /**
     * Сервис NotificationOutbox.
     */
    private final NotificationOutboxService outboxService;

    /**
     * Сервис настроек Telegram.
     */
    private final TelegramSettingsService settingsService;

    /**
     * Выполняет операцию.
     */
    public TelegramAutoRefreshService(
            NotificationOutboxService outboxService,
            TelegramSettingsService settingsService
    ) {
        this.outboxService = outboxService;
        this.settingsService = settingsService;
    }

    /**
     * Обновляет TwoweeksIfWithinRange.
     */
    public void refreshTwoweeksIfWithinRange(OffsetDateTime startAt) {
        if (!isWithinTwoWeeks(startAt)) {
            return;
        }

        /**
         * Ставит в очередь ScheduleCommand.
         */
        enqueueScheduleCommand(TelegramNotificationCommand.REFRESH_TWOWEEKS);
    }

    /**
     * Обновляет EventsIfWithinRange.
     */
    public void refreshEventsIfWithinRange(OffsetDateTime startAt) {
        if (!isWithinTwoWeeks(startAt)) {
            return;
        }

        /**
         * Обновляет Events.
         */
        refreshEvents();
    }

    /**
     * Обновляет Events.
     */
    public void refreshEvents() {
        settingsService.getAny().ifPresentOrElse(settings -> {
            ChatRouting routing = new ChatRouting(settings.getChatId(), settings.getScheduleThreadId());
            outboxService.enqueue(NotificationTarget.TELEGRAM, routing, TelegramNotificationCommand.REFRESH_EVENTS);
        }, () -> log.warn("Пропуск авто-обновления: настройки Telegram не заданы"));
    }

    /**
     * Ставит в очередь ScheduleCommand.
     */
    private void enqueueScheduleCommand(String command) {
        settingsService.getAny().ifPresentOrElse(settings -> {
            ChatRouting routing = new ChatRouting(settings.getChatId(), settings.getScheduleThreadId());
            outboxService.enqueue(NotificationTarget.TELEGRAM, routing, command);
        }, () -> log.warn("Пропуск авто-обновления: настройки Telegram не заданы"));
    }

    /**
     * Проверяет WithinTwoWeeks.
     */
    private boolean isWithinTwoWeeks(OffsetDateTime startAt) {
        if (startAt == null) {
            return false;
        }
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime limit = now.plusWeeks(2);
        return !startAt.isBefore(now) && !startAt.isAfter(limit);
    }
}
