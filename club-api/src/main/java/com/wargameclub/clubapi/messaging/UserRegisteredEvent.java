package com.wargameclub.clubapi.messaging;

import java.time.OffsetDateTime;

/**
 * Событие для UserRegistered.
 */
public record UserRegisteredEvent(
        Long userId,
        String name,
        OffsetDateTime registeredAt
) {
}
