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

/**
 * Сервис агрегации и сохранения аналитики по событиям и покупкам.
 */
@Service
public class AnalyticsService {
    /**
     * Объект синхронизации для потокобезопасного обновления агрегатов.
     */
    private final Object lock = new Object();
    /**
     * Счетчики покупок по категориям/типам мероприятий.
     */
    private final Map<String, Integer> purchasesByCategory = new HashMap<>();
    /**
     * Счетчики покупок по идентификаторам мероприятий.
     */
    private final Map<Long, Integer> purchasesByEvent = new HashMap<>();
    /**
     * Справочник данных по мероприятиям для аналитики.
     */
    private final Map<Long, EventInfo> eventInfo = new HashMap<>();

    /**
     * Общая сумма выручки.
     */
    private BigDecimal revenue = BigDecimal.ZERO;

    /**
     * Время последнего обновления аналитики.
     */
    private OffsetDateTime lastUpdated;

    /**
     * Сериализатор JSON.
     */
    private final ObjectMapper objectMapper;

    /**
     * Путь к файлу сохранения аналитики (опционально).
     */
    private final Path analyticsFile;

    /**
     * Создает сервис аналитики.
     *
     * @param objectMapper сериализатор JSON
     * @param analyticsFile путь к файлу аналитики из настроек приложения
     */
    public AnalyticsService(ObjectMapper objectMapper, @Value("${app.analytics.file:}") String analyticsFile) {
        this.objectMapper = objectMapper.copy().findAndRegisterModules();
        this.analyticsFile = analyticsFile == null || analyticsFile.isBlank() ? null : Path.of(analyticsFile);
    }

    /**
     * Фиксирует покупку билетов и обновляет агрегаты.
     *
     * @param event событие покупки билетов
     */
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

    /**
     * Фиксирует обновление мероприятия для аналитики.
     *
     * @param event событие обновления мероприятия
     */
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

    /**
     * Сохраняет текущий снимок аналитики в файл. Метод должен вызываться под {@code lock}.
     */
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

    /**
     * Формирует список популярных мероприятий по количеству покупок.
     * Метод должен вызываться под {@code lock}.
     *
     * @return список топ-мероприятий
     */
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

    /**
     * Метаданные мероприятия для аналитики.
     *
     * @param title название мероприятия
     * @param type тип мероприятия
     */
    private record EventInfo(String title, EventType type) {
    }
}
