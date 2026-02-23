package com.wargameclub.clubapi;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wargameclub.clubapi.enums.EventStatus;
import com.wargameclub.clubapi.enums.EventType;
import com.wargameclub.clubapi.messaging.AnalyticsService;
import com.wargameclub.clubapi.messaging.AnalyticsSnapshot;
import com.wargameclub.clubapi.messaging.EventUpdatedEvent;
import com.wargameclub.clubapi.messaging.TicketPurchasedEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Класс модуля club-api.
 */
public class AnalyticsServiceTest {

    /**
     * Выполняет операцию.
     */
    @Test
    void writesSnapshotToFile() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Path file = Files.createTempFile("analytics", ".json");
        AnalyticsService service = new AnalyticsService(mapper, file.toString());

        /**
         * Выполняет операцию.
         */
        TicketPurchasedEvent purchase = new TicketPurchasedEvent(
                1L,
                "Event",
                EventType.OTHER,
                10L,
                "Buyer",
                2,
                new BigDecimal("1500"),
                OffsetDateTime.now()
        );
        service.recordPurchase(purchase);

        /**
         * Выполняет операцию.
         */
        EventUpdatedEvent updated = new EventUpdatedEvent(
                1L,
                "Event",
                EventType.OTHER,
                EventStatus.SCHEDULED,
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(2),
                OffsetDateTime.now()
        );
        service.recordEventUpdated(updated);

        AnalyticsSnapshot snapshot = mapper.readValue(file.toFile(), AnalyticsSnapshot.class);

        /**
         * Выполняет операцию.
         */
        assertEquals(new BigDecimal("1500"), snapshot.revenue());

        /**
         * Выполняет операцию.
         */
        assertEquals(2, snapshot.purchasesByCategory().get("OTHER"));
        assertTrue(snapshot.topEvents().stream().anyMatch(event -> event.eventId().equals(1L)));

        /**
         * Выполняет операцию.
         */
        assertNotNull(snapshot.lastUpdated());
    }
}
