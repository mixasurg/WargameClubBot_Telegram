package com.wargameclub.clubapi.lab.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Запрос на покупку билета в лабораторном контуре.
 */
public record LabTicketPurchaseRequest(
        @NotNull @Positive Long userId,
        @NotBlank String ticketCode,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount
) {
}
