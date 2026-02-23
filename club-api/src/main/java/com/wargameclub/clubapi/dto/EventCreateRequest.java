package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO запроса на создание мероприятия.
 */
public record EventCreateRequest(
        @NotBlank String title,
        @NotNull EventType type,
        String description,
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt,
        @NotNull @Positive Long organizerUserId,
        Integer capacity
) {
}

