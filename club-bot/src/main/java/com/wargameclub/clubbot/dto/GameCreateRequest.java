package com.wargameclub.clubbot.dto;

public record GameCreateRequest(
        String name,
        int defaultDurationMinutes,
        int tableUnits
) {
}

