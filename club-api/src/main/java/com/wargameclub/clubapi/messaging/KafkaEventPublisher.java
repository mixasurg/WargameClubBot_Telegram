package com.wargameclub.clubapi.messaging;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Публикатор доменных событий в Kafka с отправкой после commit транзакции.
 */
@Service
public class KafkaEventPublisher {
    /**
     * Логгер.
     */
    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    /**
     * Kafka-шаблон для отправки сообщений.
     */
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Создает публикатор Kafka-событий.
     *
     * @param kafkaTemplate Kafka-шаблон
     */
    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Публикует событие покупки билета.
     *
     * @param event событие покупки билета
     */
    public void publishTicketPurchased(TicketPurchasedEvent event) {
        send(KafkaTopics.TICKET_PURCHASED, event.eventId(), event);
    }

    /**
     * Публикует событие отмены билета.
     *
     * @param event событие отмены билета
     */
    public void publishTicketCancelled(TicketCancelledEvent event) {
        send(KafkaTopics.TICKET_CANCELLED, event.eventId(), event);
    }

    /**
     * Публикует событие обновления мероприятия.
     *
     * @param event событие обновления мероприятия
     */
    public void publishEventUpdated(EventUpdatedEvent event) {
        send(KafkaTopics.EVENT_UPDATED, event.eventId(), event);
    }

    /**
     * Публикует событие создания бронирования.
     *
     * @param event событие создания бронирования
     */
    public void publishBookingCreated(BookingCreatedEvent event) {
        send(KafkaTopics.BOOKING_CREATED, event.bookingId(), event);
    }

    /**
     * Публикует событие отмены бронирования.
     *
     * @param event событие отмены бронирования
     */
    public void publishBookingCancelled(BookingCancelledEvent event) {
        send(KafkaTopics.BOOKING_CANCELLED, event.bookingId(), event);
    }

    /**
     * Публикует событие регистрации пользователя.
     *
     * @param event событие регистрации пользователя
     */
    public void publishUserRegistered(UserRegisteredEvent event) {
        send(KafkaTopics.USER_REGISTERED, event.userId(), event);
    }

    /**
     * Отправляет сообщение в Kafka немедленно либо после commit активной транзакции.
     *
     * @param topic топик Kafka
     * @param key ключ сообщения
     * @param payload полезная нагрузка
     */
    private void send(String topic, Long key, Object payload) {
        String keyValue = Optional.ofNullable(key).map(String::valueOf).orElse(null);
        Runnable sendAction = () -> safeSend(topic, keyValue, payload);
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

    /**
     * Отправляет сообщение в Kafka без проброса исключений в вызывающий слой.
     *
     * @param topic топик Kafka
     * @param key ключ сообщения
     * @param payload полезная нагрузка
     */
    private void safeSend(String topic, String key, Object payload) {
        try {
            kafkaTemplate.send(topic, key, payload).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Не удалось отправить Kafka-событие в топик {}", topic, ex);
                }
            });
        } catch (Exception ex) {
            log.error("Не удалось отправить Kafka-событие в топик {}", topic, ex);
        }
    }
}
