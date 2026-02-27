package com.wargameclub.clubapi.dto;

import java.time.OffsetDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Запрос на фиксацию использования армии.
 *
 * @param usedByUserId идентификатор пользователя, использовавшего армию
 * @param usedAt дата и время использования
 * @param notes дополнительная заметка (опционально)
 */
public record ArmyUsageRequest(
        @NotNull @Positive Long usedByUserId,
        @NotNull OffsetDateTime usedAt,
        String notes
) {
}
