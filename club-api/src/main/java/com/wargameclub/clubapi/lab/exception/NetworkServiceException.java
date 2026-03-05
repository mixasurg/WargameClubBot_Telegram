package com.wargameclub.clubapi.lab.exception;

/**
 * Эмуляция сетевой ошибки при вызове внешнего сервиса.
 */
public class NetworkServiceException extends LabServiceException {

    public NetworkServiceException(String message) {
        super(message);
    }
}
