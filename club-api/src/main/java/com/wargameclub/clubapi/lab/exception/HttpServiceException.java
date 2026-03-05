package com.wargameclub.clubapi.lab.exception;

/**
 * Ошибка внешнего сервиса со статусом HTTP.
 */
public class HttpServiceException extends LabServiceException {

    private final int status;

    public HttpServiceException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
