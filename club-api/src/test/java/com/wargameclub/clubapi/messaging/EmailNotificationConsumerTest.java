package com.wargameclub.clubapi.messaging;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationConsumerTest {

    @Mock
    private Acknowledgment acknowledgment;

    private EmailNotificationConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new EmailNotificationConsumer();
    }

    @Test
    void onTicketPurchasedAcknowledgesValidEvent() {
        TicketPurchasedEvent event = new TicketPurchasedEvent(
                1L,
                "Event",
                null,
                10L,
                "Buyer",
                2,
                new BigDecimal("100"),
                OffsetDateTime.now()
        );

        consumer.onTicketPurchased(event, acknowledgment);

        verify(acknowledgment).acknowledge();
    }

    @Test
    void onTicketPurchasedRejectsInvalidEvent() {
        TicketPurchasedEvent event = new TicketPurchasedEvent(
                1L,
                null,
                null,
                10L,
                null,
                1,
                BigDecimal.ZERO,
                OffsetDateTime.now()
        );

        assertThatThrownBy(() -> consumer.onTicketPurchased(event, acknowledgment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ticket.purchased");
    }
}
