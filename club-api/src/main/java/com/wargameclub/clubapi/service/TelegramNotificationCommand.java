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
     * Префикс команды запроса подтверждения участия в мероприятии.
     */
    public static final String EVENT_ATTENDANCE_PROMPT = "__cmd:event_attendance_prompt__";

    /**
     * Формирует команду запроса подтверждения участия в мероприятии.
     *
     * @param eventId идентификатор мероприятия
     * @param eventTitle название мероприятия (опционально)
     * @return строка команды для Telegram-бота
     */
    public static String eventAttendancePrompt(Long eventId, String eventTitle) {
        String title = eventTitle == null ? "" : eventTitle.replace('\n', ' ').replace('\r', ' ').trim();
        if (title.isEmpty()) {
            return EVENT_ATTENDANCE_PROMPT + ":" + eventId;
        }
        return EVENT_ATTENDANCE_PROMPT + ":" + eventId + ":" + title;
    }

    /**
     * Закрытый конструктор для утилитного класса.
     */
    private TelegramNotificationCommand() {
    }
}
