package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Запрос на подтверждение/отклонение участия пользователя в мероприятии.
 *
 * @param userId идентификатор пользователя
 */
public record EventAttendanceConfirmRequest(
        @NotNull @Positive Long userId
) {
}
