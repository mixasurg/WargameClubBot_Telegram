package com.wargameclub.clubapi.enums;

/**
 * Статусы отправки уведомлений.
 */
public enum NotificationStatus {
    /**
     * Ожидает отправки.
     */
    PENDING,
    /**
     * Успешно отправлено.
     */
    SENT,
    /**
     * Ошибка отправки.
     */
    FAILED
}
