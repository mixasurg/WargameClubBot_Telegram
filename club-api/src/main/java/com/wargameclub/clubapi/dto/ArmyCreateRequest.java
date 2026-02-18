package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ArmyCreateRequest(
        @NotNull @Positive Long ownerUserId,
        @NotBlank String game,
        @NotBlank String faction,
        boolean isClubShared
) {
}

