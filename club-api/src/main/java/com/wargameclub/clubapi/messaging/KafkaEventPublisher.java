package com.wargameclub.clubapi.messaging;

import java.util.Optional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class KafkaEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTicketPurchased(TicketPurchasedEvent event) {
        send(KafkaTopics.TICKET_PURCHASED, event.eventId(), event);
    }

    public void publishTicketCancelled(TicketCancelledEvent event) {
        send(KafkaTopics.TICKET_CANCELLED, event.eventId(), event);
    }

    public void publishEventUpdated(EventUpdatedEvent event) {
        send(KafkaTopics.EVENT_UPDATED, event.eventId(), event);
    }

    public void publishBookingCreated(BookingCreatedEvent event) {
        send(KafkaTopics.BOOKING_CREATED, event.bookingId(), event);
    }

    public void publishBookingCancelled(BookingCancelledEvent event) {
        send(KafkaTopics.BOOKING_CANCELLED, event.bookingId(), event);
    }

    public void publishUserRegistered(UserRegisteredEvent event) {
        send(KafkaTopics.USER_REGISTERED, event.userId(), event);
    }

    private void send(String topic, Long key, Object payload) {
        Runnable sendAction = () -> kafkaTemplate.send(topic, Optional.ofNullable(key).map(String::valueOf).orElse(null), payload);
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendAction.run();
                }
            });
        } else {
            sendAction.run();
        }
    }
}
