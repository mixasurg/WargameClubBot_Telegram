package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO запроса для NotificationFail.
 */
public record NotificationFailRequest(
        @NotBlank String error
) {
}

