package com.wargameclub.clubbot.dto;

/**
 * DTO запроса на фиксацию результата бронирования.
 */
public record BookingResultRequest(
        Long reporterUserId,
        String outcome
) {
}
