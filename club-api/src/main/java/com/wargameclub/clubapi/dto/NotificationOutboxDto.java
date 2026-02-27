package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.wargameclub.clubapi.enums.NotificationStatus;
import com.wargameclub.clubapi.enums.NotificationTarget;

/**
 * Представление уведомления из outbox-очереди.
 *
 * @param id идентификатор уведомления
 * @param target целевой канал уведомления
 * @param chatRouting маршрут/идентификатор чата назначения
 * @param text текст уведомления
 * @param status статус отправки
 * @param attempts число попыток отправки
 * @param nextAttemptAt дата и время следующей попытки
 * @param createdAt дата и время создания
 */
public record NotificationOutboxDto(
        UUID id,
        NotificationTarget target,
        String chatRouting,
        String text,
        NotificationStatus status,
        int attempts,
        OffsetDateTime nextAttemptAt,
        OffsetDateTime createdAt
) {
}
