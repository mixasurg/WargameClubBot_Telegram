package com.wargameclub.clubapi.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsConsumer {
    private final AnalyticsService analyticsService;

    public AnalyticsConsumer(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @KafkaListener(
            topics = KafkaTopics.TICKET_PURCHASED,
            groupId = "analytics-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTicketPurchased(TicketPurchasedEvent event, Acknowledgment acknowledgment) {
        analyticsService.recordPurchase(event);
        acknowledgment.acknowledge();
    }

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
