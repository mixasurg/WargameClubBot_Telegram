package com.wargameclub.clubbot.service;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wargameclub.clubbot.client.ClubApiClient;
import com.wargameclub.clubbot.dto.NotificationOutboxDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Сервис для работы с сущностью OutboxPolling.
 */
@Component
public class OutboxPollingService {
    /**
     * Логгер.
     */
    private static final Logger log = LoggerFactory.getLogger(OutboxPollingService.class);

    /**
     * Клиент ClubApi.
     */
    private final ClubApiClient apiClient;

    /**
     * Поле состояния.
     */
    private final NotificationDispatcher dispatcher;

    /**
     * Сериализатор JSON.
     */
    private final ObjectMapper objectMapper;

    /**
     * Выполняет операцию.
     */
    public OutboxPollingService(
            ClubApiClient apiClient,
            NotificationDispatcher dispatcher,
            ObjectMapper objectMapper
    ) {
        this.apiClient = apiClient;
        this.dispatcher = dispatcher;
        this.objectMapper = objectMapper;
    }

    /**
     * Выполняет операцию.
     */
    @Scheduled(fixedDelayString = "#{${bot.poll-interval-seconds:10} * 1000}")
    public void pollOutbox() {
        List<NotificationOutboxDto> pending = apiClient.getPendingNotifications(20);
        if (pending == null || pending.isEmpty()) {
            return;
        }
        for (NotificationOutboxDto notification : pending) {
            try {
                ChatRouting routing = objectMapper.readValue(notification.chatRouting(), ChatRouting.class);
                if (routing.chatId() == null) {
                    apiClient.failNotification(notification.id(), "Отсутствует chatId в маршрутизации");
                    continue;
                }
                dispatcher.dispatch(new NotificationMessage(routing.chatId(), routing.threadId(), notification.text()));
                apiClient.ackNotification(notification.id());
            } catch (Exception ex) {
                log.warn("Не удалось отправить уведомление {}", notification.id(), ex);
                apiClient.failNotification(notification.id(), ex.getMessage() == null ? "Ошибка отправки" : ex.getMessage());
            }
        }
    }
}

