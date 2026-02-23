package com.wargameclub.clubbot.dto;

import java.util.UUID;

/**
 * DTO для NotificationOutbox.
 */
public record NotificationOutboxDto(
        UUID id,
        String chatRouting,
        String text
) {
}

