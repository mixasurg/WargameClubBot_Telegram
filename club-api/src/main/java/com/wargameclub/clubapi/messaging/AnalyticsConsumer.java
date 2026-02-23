package com.wargameclub.clubapi.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Обработчик сообщений для аналитики.
 */
@Service
public class AnalyticsConsumer {

    /**
     * Сервис аналитики.
     */
    private final AnalyticsService analyticsService;

    /**
     * Конструктор AnalyticsConsumer.
     */
    public AnalyticsConsumer(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @KafkaListener(
            topics = KafkaTopics.TICKET_PURCHASED,
            groupId = "analytics-service",
            containerFactory = "kafkaListenerContainerFactory"
    )

    /**
     * Выполняет операцию.
     */
    public void onTicketPurchased(TicketPurchasedEvent event, Acknowledgment acknowledgment) {
        analyticsService.recordPurchase(event);
        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = KafkaTopics.EVENT_UPDATED,
            groupId = "analytics-service",
            containerFactory = "kafkaListenerContainerFactory"
    )

    /**
     * Выполняет операцию.
     */
    public void onEventUpdated(EventUpdatedEvent event, Acknowledgment acknowledgment) {
        analyticsService.recordEventUpdated(event);
        acknowledgment.acknowledge();
    }
}
