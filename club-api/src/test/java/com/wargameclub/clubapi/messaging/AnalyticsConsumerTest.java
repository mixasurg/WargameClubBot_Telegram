package com.wargameclub.clubapi.messaging;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.EventStatus;
import com.wargameclub.clubapi.enums.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalyticsConsumerTest {

    @Mock
    private AnalyticsService analyticsService;
    @Mock
    private Acknowledgment acknowledgment;

    private AnalyticsConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new AnalyticsConsumer(analyticsService);
    }

    @Test
    void onTicketPurchasedDelegatesAndAcknowledges() {
        TicketPurchasedEvent event = new TicketPurchasedEvent(
                1L,
                "Event",
                EventType.OTHER,
                10L,
                "Buyer",
                2,
                new BigDecimal("100"),
                OffsetDateTime.now()
        );

        consumer.onTicketPurchased(event, acknowledgment);

        verify(analyticsService).recordPurchase(event);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void onEventUpdatedDelegatesAndAcknowledges() {
        EventUpdatedEvent event = new EventUpdatedEvent(
                1L,
                "Event",
                EventType.OTHER,
                EventStatus.SCHEDULED,
                OffsetDateTime.now(),
                OffsetDateTime.now().plusHours(2),
                OffsetDateTime.now()
        );

        consumer.onEventUpdated(event, acknowledgment);

        verify(analyticsService).recordEventUpdated(event);
        verify(acknowledgment).acknowledge();
    }
}
