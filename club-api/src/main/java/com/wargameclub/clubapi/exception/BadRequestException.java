package com.wargameclub.clubapi.exception;

/**
 * Исключение для случаев некорректного запроса (HTTP 400).
 */
public class BadRequestException extends RuntimeException {

    /**
     * Создает исключение с описанием причины.
     *
     * @param message описание ошибки для клиента
     */
    public BadRequestException(String message) {
        super(message);
    }
}
