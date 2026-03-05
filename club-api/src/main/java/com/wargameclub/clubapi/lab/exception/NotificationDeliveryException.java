package com.wargameclub.clubapi.lab.exception;

/**
 * Ошибка доставки уведомления.
 */
public class NotificationDeliveryException extends LabServiceException {

    public NotificationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotificationDeliveryException(String message) {
        super(message);
    }
}
