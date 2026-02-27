package com.wargameclub.clubapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Запрос на создание игры в каталоге.
 *
 * @param name название игры/системы
 * @param defaultDurationMinutes длительность по умолчанию в минутах
 * @param tableUnits требуемое количество единиц стола
 */
public record GameCreateRequest(
        @NotBlank String name,
        @Positive int defaultDurationMinutes,
        @Positive int tableUnits
) {
}
