package com.wargameclub.clubapi.lab.exception;

/**
 * Ошибка таймаута при оплате.
 */
public class PaymentTimeoutException extends LabServiceException {

    public PaymentTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
