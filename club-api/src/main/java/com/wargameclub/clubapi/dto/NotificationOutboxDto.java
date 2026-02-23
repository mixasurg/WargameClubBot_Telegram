package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.wargameclub.clubapi.enums.NotificationStatus;
import com.wargameclub.clubapi.enums.NotificationTarget;

/**
 * DTO для NotificationOutbox.
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

