package com.wargameclub.clubapi.messaging;

import java.time.OffsetDateTime;

/**
 * Событие регистрации пользователя.
 *
 * @param userId идентификатор пользователя
 * @param name имя пользователя
 * @param registeredAt время регистрации
 */
public record UserRegisteredEvent(
        Long userId,
        String name,
        OffsetDateTime registeredAt
) {
}
