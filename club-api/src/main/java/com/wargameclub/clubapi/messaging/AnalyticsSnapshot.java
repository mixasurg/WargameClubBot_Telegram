package com.wargameclub.clubapi.messaging;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import com.wargameclub.clubapi.enums.EventType;

/**
 * Компонент обмена сообщениями для AnalyticsSnapshot.
 */
public record AnalyticsSnapshot(
        Map<String, Integer> purchasesByCategory,
        Map<Long, Integer> purchasesByEvent,
        List<PopularEvent> topEvents,
        BigDecimal revenue,
        OffsetDateTime lastUpdated
) {

    /**
     * Событие для Popular.
     */
    public record PopularEvent(
            Long eventId,
            String title,
            EventType type,
            int purchases
    ) {
    }
}
