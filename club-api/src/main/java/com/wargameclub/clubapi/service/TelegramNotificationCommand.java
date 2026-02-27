package com.wargameclub.clubapi.service;

/**
 * Команды для служебных уведомлений Telegram-бота.
 */
public final class TelegramNotificationCommand {

    /**
     * Команда обновления расписания на две недели.
     */
    public static final String REFRESH_TWOWEEKS = "__cmd:refresh_twoweeks__";

    /**
     * Команда обновления списка мероприятий.
     */
    public static final String REFRESH_EVENTS = "__cmd:refresh_events__";

    /**
     * Закрытый конструктор для утилитного класса.
     */
    private TelegramNotificationCommand() {
    }
}
