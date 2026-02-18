package com.wargameclub.clubapi.messaging;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.EventType;

public record TicketPurchasedEvent(
        Long eventId,
        String eventTitle,
        EventType eventType,
        Long userId,
        String userName,
        int count,
        BigDecimal amount,
        OffsetDateTime occurredAt
) {
}
