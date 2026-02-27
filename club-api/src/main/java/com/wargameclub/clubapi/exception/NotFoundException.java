package com.wargameclub.clubapi.exception;

/**
 * Исключение для случаев, когда ресурс не найден (HTTP 404).
 */
public class NotFoundException extends RuntimeException {

    /**
     * Создает исключение с описанием отсутствующего ресурса.
     *
     * @param message описание ошибки для клиента
     */
    public NotFoundException(String message) {
        super(message);
    }
}
