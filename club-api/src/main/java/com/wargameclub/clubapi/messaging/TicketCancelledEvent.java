package com.wargameclub.clubapi.messaging;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.EventType;

/**
 * Событие отмены покупки билета.
 *
 * @param eventId идентификатор мероприятия
 * @param eventTitle название мероприятия
 * @param eventType тип мероприятия
 * @param userId идентификатор пользователя
 * @param userName имя пользователя
 * @param count количество отмененных билетов
 * @param amount сумма возврата/отмены
 * @param occurredAt время события
 */
public record TicketCancelledEvent(
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
