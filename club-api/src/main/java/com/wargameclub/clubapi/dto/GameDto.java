package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;

/**
 * Представление игры в каталоге.
 *
 * @param id идентификатор игры
 * @param name название игры/системы
 * @param defaultDurationMinutes длительность по умолчанию в минутах
 * @param tableUnits требуемое количество единиц стола
 * @param isActive признак активности игры
 * @param createdAt дата и время создания записи
 */
public record GameDto(
        Long id,
        String name,
        int defaultDurationMinutes,
        int tableUnits,
        boolean isActive,
        OffsetDateTime createdAt
) {
}
