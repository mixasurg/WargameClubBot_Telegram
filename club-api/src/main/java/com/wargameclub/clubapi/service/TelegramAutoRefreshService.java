package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.NotificationTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TelegramAutoRefreshService {
    private static final Logger log = LoggerFactory.getLogger(TelegramAutoRefreshService.class);

    private final NotificationOutboxService outboxService;
    private final TelegramSettingsService settingsService;

    public TelegramAutoRefreshService(
            NotificationOutboxService outboxService,
            TelegramSettingsService settingsService
    ) {
        this.outboxService = outboxService;
        this.settingsService = settingsService;
    }

    public void refreshTwoweeksIfWithinRange(OffsetDateTime startAt) {
        if (!isWithinTwoWeeks(startAt)) {
            return;
        }
        enqueueScheduleCommand(TelegramNotificationCommand.REFRESH_TWOWEEKS);
    }

    public void refreshEventsIfWithinRange(OffsetDateTime startAt) {
        if (!isWithinTwoWeeks(startAt)) {
            return;
        }
        refreshEvents();
    }

    public void refreshEvents() {
        settingsService.getAny().ifPresentOrElse(settings -> {
            ChatRouting routing = new ChatRouting(settings.getChatId(), settings.getScheduleThreadId());
            outboxService.enqueue(NotificationTarget.TELEGRAM, routing, TelegramNotificationCommand.REFRESH_EVENTS);
        }, () -> log.warn("Пропуск авто-обновления: настройки Telegram не заданы"));
    }

    private void enqueueScheduleCommand(String command) {
        settingsService.getAny().ifPresentOrElse(settings -> {
            ChatRouting routing = new ChatRouting(settings.getChatId(), settings.getScheduleThreadId());
            outboxService.enqueue(NotificationTarget.TELEGRAM, routing, command);
        }, () -> log.warn("Пропуск авто-обновления: настройки Telegram не заданы"));
    }

    private boolean isWithinTwoWeeks(OffsetDateTime startAt) {
        if (startAt == null) {
            return false;
        }
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime limit = now.plusWeeks(2);
        return !startAt.isBefore(now) && !startAt.isAfter(limit);
    }
}
