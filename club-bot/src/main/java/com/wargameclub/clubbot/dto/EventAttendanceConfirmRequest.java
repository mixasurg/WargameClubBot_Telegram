package com.wargameclub.clubbot.dto;

/**
 * Запрос на подтверждение/отклонение участия в мероприятии.
 *
 * @param userId идентификатор пользователя
 */
public record EventAttendanceConfirmRequest(
        Long userId
) {
}
