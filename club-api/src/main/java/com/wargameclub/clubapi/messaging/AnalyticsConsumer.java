package com.wargameclub.clubapi.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Kafka-консьюмер, передающий события в сервис аналитики.
 */
@Service
public class AnalyticsConsumer {

    /**
     * Сервис аналитики.
     */
    private final AnalyticsService analyticsService;

    /**
     * Создает консьюмера аналитики.
     *
     * @param analyticsService сервис аналитики
     */
    public AnalyticsConsumer(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Обрабатывает событие покупки билета и обновляет агрегаты аналитики.
     *
     * @param event событие покупки
     * @param acknowledgment подтверждение обработки сообщения
     */
    @KafkaListener(
            topics = KafkaTopics.TICKET_PURCHASED,
            groupId = "analytics-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTicketPurchased(TicketPurchasedEvent event, Acknowledgment acknowledgment) {
        analyticsService.recordPurchase(event);
        acknowledgment.acknowledge();
    }

    /**
     * Обрабатывает событие обновления мероприятия и синхронизирует аналитику.
     *
     * @param event событие обновления мероприятия
     * @param acknowledgment подтверждение обработки сообщения
     */
    @KafkaListener(
            topics = KafkaTopics.EVENT_UPDATED,
            groupId = "analytics-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onEventUpdated(EventUpdatedEvent event, Acknowledgment acknowledgment) {
        analyticsService.recordEventUpdated(event);
        acknowledgment.acknowledge();
    }
}
