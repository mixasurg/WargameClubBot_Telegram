package com.wargameclub.clubapi.messaging;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import com.wargameclub.clubapi.enums.EventType;

/**
 * Снимок аналитики для сохранения или отдачи внешним системам.
 *
 * @param purchasesByCategory количество покупок по категориям
 * @param purchasesByEvent количество покупок по мероприятиям
 * @param topEvents список наиболее популярных мероприятий
 * @param revenue суммарная выручка
 * @param lastUpdated время последнего обновления
 */
public record AnalyticsSnapshot(
        Map<String, Integer> purchasesByCategory,
        Map<Long, Integer> purchasesByEvent,
        List<PopularEvent> topEvents,
        BigDecimal revenue,
        OffsetDateTime lastUpdated
) {

    /**
     * Популярное мероприятие в аналитике.
     *
     * @param eventId идентификатор мероприятия
     * @param title название мероприятия
     * @param type тип мероприятия
     * @param purchases количество покупок
     */
    public record PopularEvent(
            Long eventId,
            String title,
            EventType type,
            int purchases
    ) {
    }
}
