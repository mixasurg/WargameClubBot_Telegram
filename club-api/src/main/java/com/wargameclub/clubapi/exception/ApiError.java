package com.wargameclub.clubapi.exception;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Класс модуля club-api.
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

