package com.wargameclub.clubapi.exception;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * API-ошибка, возвращаемая клиенту при обработке исключений.
 *
 * @param timestamp время формирования ошибки
 * @param status HTTP-статус
 * @param error человекочитаемое название статуса
 * @param message сообщение об ошибке
 * @param path путь запроса
 * @param details список деталей (например, ошибки валидации), может быть null
 */
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
}
