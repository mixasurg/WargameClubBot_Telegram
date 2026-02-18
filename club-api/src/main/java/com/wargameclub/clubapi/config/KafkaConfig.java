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

@Configuration
@EnableKafka
public class KafkaConfig {
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

    @Bean
    public CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(KafkaTopics.dlt(record.topic()), record.partition())
        );
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 2L));
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }

    @Bean
    public NewTopic ticketPurchasedTopic() {
        return new NewTopic(KafkaTopics.TICKET_PURCHASED, 1, (short) 1);
    }

    @Bean
    public NewTopic ticketCancelledTopic() {
        return new NewTopic(KafkaTopics.TICKET_CANCELLED, 1, (short) 1);
    }

    @Bean
    public NewTopic eventUpdatedTopic() {
        return new NewTopic(KafkaTopics.EVENT_UPDATED, 1, (short) 1);
    }

    @Bean
    public NewTopic bookingCreatedTopic() {
        return new NewTopic(KafkaTopics.BOOKING_CREATED, 1, (short) 1);
    }

    @Bean
    public NewTopic bookingCancelledTopic() {
        return new NewTopic(KafkaTopics.BOOKING_CANCELLED, 1, (short) 1);
    }

    @Bean
    public NewTopic userRegisteredTopic() {
        return new NewTopic(KafkaTopics.USER_REGISTERED, 1, (short) 1);
    }

    @Bean
    public NewTopic ticketPurchasedDltTopic() {
        return new NewTopic(KafkaTopics.dlt(KafkaTopics.TICKET_PURCHASED), 1, (short) 1);
    }

    @Bean
    public NewTopic ticketCancelledDltTopic() {
        return new NewTopic(KafkaTopics.dlt(KafkaTopics.TICKET_CANCELLED), 1, (short) 1);
    }

    @Bean
    public NewTopic eventUpdatedDltTopic() {
        return new NewTopic(KafkaTopics.dlt(KafkaTopics.EVENT_UPDATED), 1, (short) 1);
    }

    @Bean
    public NewTopic bookingCreatedDltTopic() {
        return new NewTopic(KafkaTopics.dlt(KafkaTopics.BOOKING_CREATED), 1, (short) 1);
    }

    @Bean
    public NewTopic bookingCancelledDltTopic() {
        return new NewTopic(KafkaTopics.dlt(KafkaTopics.BOOKING_CANCELLED), 1, (short) 1);
    }

    @Bean
    public NewTopic userRegisteredDltTopic() {
        return new NewTopic(KafkaTopics.dlt(KafkaTopics.USER_REGISTERED), 1, (short) 1);
    }
}
