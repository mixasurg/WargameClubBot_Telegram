package com.wargameclub.clubapi.config;

import com.wargameclub.clubapi.messaging.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Конфигурация Kafka: слушатели с ручным подтверждением, обработчик ошибок и топики приложения.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    /**
     * Создает фабрику контейнеров слушателей Kafka с ручным подтверждением и обработчиком ошибок.
     *
     * @param consumerFactory фабрика консьюмеров
     * @param kafkaErrorHandler общий обработчик ошибок слушателей
     * @return настроенная фабрика контейнеров слушателей
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            CommonErrorHandler kafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        return factory;
    }

    /**
     * Создает обработчик ошибок, перенаправляющий сообщения в DLT и ограничивающий повторы.
     *
     * @param kafkaTemplate шаблон для публикации сообщений в Kafka
     * @return обработчик ошибок для слушателей Kafka
     */
    @Bean
    public CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        // Публикует сообщения в DLT-топики, соответствующие исходной теме.
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(KafkaTopics.dlt(record.topic()), record.partition())
        );
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 2L));
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }

    /**
     * Регистрирует топик для события покупки билета.
     *
     * @return топик {@link KafkaTopics#TICKET_PURCHASED}
     */
    @Bean
    public NewTopic ticketPurchasedTopic() {
        return new NewTopic(KafkaTopics.TICKET_PURCHASED, 1, (short) 1);
    }

    /**
     * Регистрирует топик для события отмены билета.
     *
     * @return топик {@link KafkaTopics#TICKET_CANCELLED}
     */
    @Bean
    public NewTopic ticketCancelledTopic() {
        return new NewTopic(KafkaTopics.TICKET_CANCELLED, 1, (short) 1);
    }

    /**
     * Регистрирует топик для события обновления мероприятия.
     *
     * @return топик {@link KafkaTopics#EVENT_UPDATED}
     */
    @Bean
    public NewTopic eventUpdatedTopic() {
        return new NewTopic(KafkaTopics.EVENT_UPDATED, 1, (short) 1);
    }

    /**
     * Регистрирует топик для события создания бронирования.
     *
     * @return топик {@link KafkaTopics#BOOKING_CREATED}
     */
    @Bean
    public NewTopic bookingCreatedTopic() {
        return new NewTopic(KafkaTopics.BOOKING_CREATED, 1, (short) 1);
    }

    /**
     * Регистрирует топик для события отмены бронирования.
     *
     * @return топик {@link KafkaTopics#BOOKING_CANCELLED}
     */
    @Bean
    public NewTopic bookingCancelledTopic() {
        return new NewTopic(KafkaTopics.BOOKING_CANCELLED, 1, (short) 1);
    }

    /**
     * Регистрирует топик для события регистрации пользователя.
     *
     * @return топик {@link KafkaTopics#USER_REGISTERED}
     */
    @Bean
    public NewTopic userRegisteredTopic() {
        return new NewTopic(KafkaTopics.USER_REGISTERED, 1, (short) 1);
    }

    /**
     * Регистрирует DLT-топик для сообщений о покупке билета.
     *
     * @return DLT-топик для {@link KafkaTopics#TICKET_PURCHASED}
     */
    @Bean
    public NewTopic ticketPurchasedDltTopic() {
        return new NewTopic(KafkaTopics.dlt(KafkaTopics.TICKET_PURCHASED), 1, (short) 1);
    }

    /**
     * Регистрирует DLT-топик для сообщений об отмене билета.
     *
     * @return DLT-топик для {@link KafkaTopics#TICKET_CANCELLED}
     */
    @Bean
    public NewTopic ticketCancelledDltTopic() {
        return new NewTopic(KafkaTopics.dlt(KafkaTopics.TICKET_CANCELLED), 1, (short) 1);
    }

    /**
     * Регистрирует DLT-топик для сообщений об обновлении мероприятия.
     *
     * @return DLT-топик для {@link KafkaTopics#EVENT_UPDATED}
     */
    @Bean
    public NewTopic eventUpdatedDltTopic() {
        return new NewTopic(KafkaTopics.dlt(KafkaTopics.EVENT_UPDATED), 1, (short) 1);
    }

    /**
     * Регистрирует DLT-топик для сообщений о создании бронирования.
     *
     * @return DLT-топик для {@link KafkaTopics#BOOKING_CREATED}
     */
    @Bean
    public NewTopic bookingCreatedDltTopic() {
        return new NewTopic(KafkaTopics.dlt(KafkaTopics.BOOKING_CREATED), 1, (short) 1);
    }

    /**
     * Регистрирует DLT-топик для сообщений об отмене бронирования.
     *
     * @return DLT-топик для {@link KafkaTopics#BOOKING_CANCELLED}
     */
    @Bean
    public NewTopic bookingCancelledDltTopic() {
        return new NewTopic(KafkaTopics.dlt(KafkaTopics.BOOKING_CANCELLED), 1, (short) 1);
    }

    /**
     * Регистрирует DLT-топик для сообщений о регистрации пользователя.
     *
     * @return DLT-топик для {@link KafkaTopics#USER_REGISTERED}
     */
    @Bean
    public NewTopic userRegisteredDltTopic() {
        return new NewTopic(KafkaTopics.dlt(KafkaTopics.USER_REGISTERED), 1, (short) 1);
    }
}
