package com.wargameclub.clubapi.messaging;

/**
 * Компонент обмена сообщениями для KafkaTopics.
 */
public final class KafkaTopics {

    /**
     * Поле состояния.
     */
    public static final String TICKET_PURCHASED = "ticket.purchased";

    /**
     * Поле состояния.
     */
    public static final String TICKET_CANCELLED = "ticket.cancelled";

    /**
     * Поле состояния.
     */
    public static final String BOOKING_CREATED = "booking.created";

    /**
     * Поле состояния.
     */
    public static final String BOOKING_CANCELLED = "booking.cancelled";

    /**
     * Поле состояния.
     */
    public static final String EVENT_UPDATED = "event.updated";

    /**
     * Поле состояния.
     */
    public static final String USER_REGISTERED = "user.registered";

    /**
     * Поле состояния.
     */
    public static final String DLT_SUFFIX = ".dlt";

    /**
     * Конструктор KafkaTopics.
     */
    private KafkaTopics() {
    }

    /**
     * Выполняет операцию.
     */
    public static String dlt(String topic) {
        return topic + DLT_SUFFIX;
    }
}
