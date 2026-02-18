package com.wargameclub.clubapi.messaging;

import java.time.OffsetDateTime;

public record UserRegisteredEvent(
        Long userId,
        String name,
        OffsetDateTime registeredAt
) {
}
