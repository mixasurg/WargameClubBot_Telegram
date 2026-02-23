package com.wargameclub.clubapi.messaging;

import java.util.Optional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Компонент обмена сообщениями для KafkaEventPublisher.
 */
@Service
public class KafkaEventPublisher {

    /**
     * Поле состояния.
     */
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Конструктор KafkaEventPublisher.
     */
    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Публикует TicketPurchased.
     */
    public void publishTicketPurchased(TicketPurchasedEvent event) {

        /**
         * Отправляет KafkaEventPublisher.
         */
        send(KafkaTopics.TICKET_PURCHASED, event.eventId(), event);
    }

    /**
     * Публикует TicketCancelled.
     */
    public void publishTicketCancelled(TicketCancelledEvent event) {

        /**
         * Отправляет KafkaEventPublisher.
         */
        send(KafkaTopics.TICKET_CANCELLED, event.eventId(), event);
    }

    /**
     * Публикует EventUpdated.
     */
    public void publishEventUpdated(EventUpdatedEvent event) {

        /**
         * Отправляет KafkaEventPublisher.
         */
        send(KafkaTopics.EVENT_UPDATED, event.eventId(), event);
    }

    /**
     * Публикует BookingCreated.
     */
    public void publishBookingCreated(BookingCreatedEvent event) {

        /**
         * Отправляет KafkaEventPublisher.
         */
        send(KafkaTopics.BOOKING_CREATED, event.bookingId(), event);
    }

    /**
     * Публикует BookingCancelled.
     */
    public void publishBookingCancelled(BookingCancelledEvent event) {

        /**
         * Отправляет KafkaEventPublisher.
         */
        send(KafkaTopics.BOOKING_CANCELLED, event.bookingId(), event);
    }

    /**
     * Публикует UserRegistered.
     */
    public void publishUserRegistered(UserRegisteredEvent event) {

        /**
         * Отправляет KafkaEventPublisher.
         */
        send(KafkaTopics.USER_REGISTERED, event.userId(), event);
    }

    /**
     * Отправляет KafkaEventPublisher.
     */
    private void send(String topic, Long key, Object payload) {
        Runnable sendAction = () -> kafkaTemplate.send(topic, Optional.ofNullable(key).map(String::valueOf).orElse(null), payload);
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                /**
                 * Выполняет операцию.
                 */
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
