package com.wargameclub.clubapi.service;

/**
 * Сервис для работы с сущностью TelegramNotificationCommand.
 */
public final class TelegramNotificationCommand {

    /**
     * Поле состояния.
     */
    public static final String REFRESH_TWOWEEKS = "__cmd:refresh_twoweeks__";

    /**
     * Поле состояния.
     */
    public static final String REFRESH_EVENTS = "__cmd:refresh_events__";

    /**
     * Конструктор TelegramNotificationCommand.
     */
    private TelegramNotificationCommand() {
    }
}
