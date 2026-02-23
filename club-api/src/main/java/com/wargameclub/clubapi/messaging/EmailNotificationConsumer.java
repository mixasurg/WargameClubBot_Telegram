package com.wargameclub.clubapi.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Обработчик сообщений для EmailNotification.
 */
@Service
public class EmailNotificationConsumer {
    /**
     * Логгер.
     */
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationConsumer.class);

    @KafkaListener(
            topics = KafkaTopics.TICKET_PURCHASED,
            groupId = "email-notifier",
            containerFactory = "kafkaListenerContainerFactory"
    )

    /**
     * Выполняет операцию.
     */
    public void onTicketPurchased(TicketPurchasedEvent event, Acknowledgment acknowledgment) {
        if (event == null || event.userName() == null || event.eventTitle() == null) {
            throw new IllegalArgumentException("Некорректные данные события ticket.purchased");
        }
        String message = "Уважаемый " + event.userName()
                + ", вы купили " + event.count()
                + " билетов на " + event.eventTitle()
                + ". Сумма: " + event.amount();
        log.info("EMAIL уведомление: {}", message);
        acknowledgment.acknowledge();
    }
}
