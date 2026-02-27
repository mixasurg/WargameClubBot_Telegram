package com.wargameclub.clubapi.dto;

/**
 * Представление игрового стола.
 *
 * @param id идентификатор стола
 * @param name название стола
 * @param isActive признак активности стола
 * @param notes дополнительные примечания
 */
public record TableDto(
        Long id,
        String name,
        boolean isActive,
        String notes
) {
}
