package com.wargameclub.clubapi.messaging;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wargameclub.clubapi.enums.EventType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
    private final Object lock = new Object();
    private final Map<String, Integer> purchasesByCategory = new HashMap<>();
    private final Map<Long, Integer> purchasesByEvent = new HashMap<>();
    private final Map<Long, EventInfo> eventInfo = new HashMap<>();
    private BigDecimal revenue = BigDecimal.ZERO;
    private OffsetDateTime lastUpdated;

    private final ObjectMapper objectMapper;
    private final Path analyticsFile;

    public AnalyticsService(ObjectMapper objectMapper, @Value("${app.analytics.file:}") String analyticsFile) {
        this.objectMapper = objectMapper;
        this.analyticsFile = analyticsFile == null || analyticsFile.isBlank() ? null : Path.of(analyticsFile);
    }

    public void recordPurchase(TicketPurchasedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Событие ticket.purchased отсутствует");
        }
        int count = Math.max(event.count(), 0);
        BigDecimal amount = event.amount() == null ? BigDecimal.ZERO : event.amount();
        String category = event.eventType() == null ? "UNKNOWN" : event.eventType().name();
        synchronized (lock) {
            purchasesByCategory.merge(category, count, Integer::sum);
            if (event.eventId() != null) {
                purchasesByEvent.merge(event.eventId(), count, Integer::sum);
                eventInfo.putIfAbsent(event.eventId(), new EventInfo(event.eventTitle(), event.eventType()));
            }
            revenue = revenue.add(amount);
            lastUpdated = event.occurredAt();
            persistLocked();
        }
    }

    public void recordEventUpdated(EventUpdatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Событие event.updated отсутствует");
        }
        synchronized (lock) {
            if (event.eventId() != null) {
                eventInfo.put(event.eventId(), new EventInfo(event.title(), event.type()));
            }
            lastUpdated = event.updatedAt();
            persistLocked();
        }
    }

    private void persistLocked() {
        if (analyticsFile == null) {
            return;
        }
        try {
            Path parent = analyticsFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(
                    analyticsFile.toFile(),
                    new AnalyticsSnapshot(
                            new HashMap<>(purchasesByCategory),
                            new HashMap<>(purchasesByEvent),
                            buildTopEventsLocked(),
                            revenue,
                            lastUpdated
                    )
            );
        } catch (IOException ex) {
            throw new IllegalStateException("Не удалось сохранить аналитику", ex);
        }
    }

    private List<AnalyticsSnapshot.PopularEvent> buildTopEventsLocked() {
        List<AnalyticsSnapshot.PopularEvent> result = new ArrayList<>();
        purchasesByEvent.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .forEach(entry -> {
                    EventInfo info = eventInfo.get(entry.getKey());
                    String title = info == null ? null : info.title();
                    EventType type = info == null ? null : info.type();
                    result.add(new AnalyticsSnapshot.PopularEvent(entry.getKey(), title, type, entry.getValue()));
                });
        return result;
    }

    private record EventInfo(String title, EventType type) {
    }
}
