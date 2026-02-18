package com.wargameclub.clubapi.messaging;

public final class KafkaTopics {
    public static final String TICKET_PURCHASED = "ticket.purchased";
    public static final String TICKET_CANCELLED = "ticket.cancelled";
    public static final String BOOKING_CREATED = "booking.created";
    public static final String BOOKING_CANCELLED = "booking.cancelled";
    public static final String EVENT_UPDATED = "event.updated";
    public static final String USER_REGISTERED = "user.registered";
    public static final String DLT_SUFFIX = ".dlt";

    private KafkaTopics() {
    }

    public static String dlt(String topic) {
        return topic + DLT_SUFFIX;
    }
}
