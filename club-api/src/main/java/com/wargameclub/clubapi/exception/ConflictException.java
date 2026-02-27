package com.wargameclub.clubapi.exception;

/**
 * Исключение для конфликтующих запросов (HTTP 409).
 */
public class ConflictException extends RuntimeException {

    /**
     * Создает исключение с описанием причины конфликта.
     *
     * @param message описание ошибки для клиента
     */
    public ConflictException(String message) {
        super(message);
    }
}
