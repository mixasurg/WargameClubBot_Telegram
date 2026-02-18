package com.wargameclub.clubbot.dto;

public record BookingResultRequest(
        Long reporterUserId,
        String outcome
) {
}
