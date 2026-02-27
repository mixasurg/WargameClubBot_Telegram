package com.wargameclub.clubapi.messaging;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new KafkaEventPublisher(kafkaTemplate);
    }

    @Test
    void publishBookingCreatedSendsImmediatelyWithoutTransaction() {
        BookingCreatedEvent event = new BookingCreatedEvent(10L);

        publisher.publishBookingCreated(event);

        verify(kafkaTemplate).send(KafkaTopics.BOOKING_CREATED, "10", event);
    }

    @Test
    void publishBookingCreatedDefersUntilAfterCommit() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            BookingCreatedEvent event = new BookingCreatedEvent(20L);

            publisher.publishBookingCreated(event);

            verify(kafkaTemplate, never()).send(anyString(), any(), any());
            List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
            assertThat(syncs).hasSize(1);
            syncs.forEach(TransactionSynchronization::afterCommit);

            verify(kafkaTemplate).send(KafkaTopics.BOOKING_CREATED, "20", event);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }
}
