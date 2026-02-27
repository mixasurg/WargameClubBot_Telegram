package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.NotificationTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Сервис автообновления сообщений Telegram при изменениях расписания/мероприятий.
 */
@Service
public class TelegramAutoRefreshService {
    /**
     * Логгер.
     */
    private static final Logger log = LoggerFactory.getLogger(TelegramAutoRefreshService.class);

    /**
     * Сервис outbox-уведомлений.
     */
    private final NotificationOutboxService outboxService;

    /**
     * Сервис настроек Telegram.
     */
    private final TelegramSettingsService settingsService;

    /**
     * Создает сервис автообновления Telegram.
     *
     * @param outboxService сервис outbox
     * @param settingsService сервис настроек Telegram
     */
    public TelegramAutoRefreshService(
            NotificationOutboxService outboxService,
            TelegramSettingsService settingsService
    ) {
        this.outboxService = outboxService;
        this.settingsService = settingsService;
    }

    /**
     * Ставит задачу обновления двухнедельного расписания, если старт в пределах 2 недель.
     *
     * @param startAt время начала события
     */
    public void refreshTwoweeksIfWithinRange(OffsetDateTime startAt) {
        if (!isWithinTwoWeeks(startAt)) {
            return;
        }
        enqueueScheduleCommand(TelegramNotificationCommand.REFRESH_TWOWEEKS);
    }

    /**
     * Обновляет список мероприятий, если старт в пределах 2 недель.
     *
     * @param startAt время начала события
     */
    public void refreshEventsIfWithinRange(OffsetDateTime startAt) {
        if (!isWithinTwoWeeks(startAt)) {
            return;
        }
        refreshEvents();
    }

    /**
     * Ставит в очередь команду обновления списка мероприятий.
     */
    public void refreshEvents() {
        settingsService.getAny().ifPresentOrElse(settings -> {
            ChatRouting routing = new ChatRouting(settings.getChatId(), settings.getScheduleThreadId());
            outboxService.enqueue(NotificationTarget.TELEGRAM, routing, TelegramNotificationCommand.REFRESH_EVENTS);
        }, () -> log.warn("Пропуск авто-обновления: настройки Telegram не заданы"));
    }

    /**
     * Ставит в очередь команду обновления расписания.
     *
     * @param command команда обновления
     */
    private void enqueueScheduleCommand(String command) {
        settingsService.getAny().ifPresentOrElse(settings -> {
            ChatRouting routing = new ChatRouting(settings.getChatId(), settings.getScheduleThreadId());
            outboxService.enqueue(NotificationTarget.TELEGRAM, routing, command);
        }, () -> log.warn("Пропуск авто-обновления: настройки Telegram не заданы"));
    }

    /**
     * Проверяет, находится ли время события в пределах ближайших двух недель.
     *
     * @param startAt время начала события
     * @return true, если в пределах 2 недель
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
