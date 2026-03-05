package com.wargameclub.clubapi.lab.exception;

/**
 * Базовое исключение эмуляции внешних сервисов.
 */
public class LabServiceException extends RuntimeException {

    public LabServiceException(String message) {
        super(message);
    }

    public LabServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
