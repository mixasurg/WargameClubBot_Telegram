package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationFailRequest(
        @NotBlank String error
) {
}

