package com.wargameclub.clubapi.messaging;

/**
 * Константы имен Kafka-топиков и утилита для DLT.
 */
public final class KafkaTopics {

    /**
     * Топик события покупки билета.
     */
    public static final String TICKET_PURCHASED = "ticket.purchased";

    /**
     * Топик события отмены билета.
     */
    public static final String TICKET_CANCELLED = "ticket.cancelled";

    /**
     * Топик события создания бронирования.
     */
    public static final String BOOKING_CREATED = "booking.created";

    /**
     * Топик события отмены бронирования.
     */
    public static final String BOOKING_CANCELLED = "booking.cancelled";

    /**
     * Топик события обновления мероприятия.
     */
    public static final String EVENT_UPDATED = "event.updated";

    /**
     * Топик события регистрации пользователя.
     */
    public static final String USER_REGISTERED = "user.registered";

    /**
     * Суффикс dead-letter топиков.
     */
    public static final String DLT_SUFFIX = ".dlt";

    /**
     * Закрытый конструктор для утилитного класса.
     */
    private KafkaTopics() {
    }

    /**
     * Формирует имя DLT-топика для указанного топика.
     *
     * @param topic исходный топик
     * @return имя DLT-топика
     */
    public static String dlt(String topic) {
        return topic + DLT_SUFFIX;
    }
}
