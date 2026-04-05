package com.wargameclub.clubapi.messaging;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.entity.EventRegistration;
import com.wargameclub.clubapi.enums.NotificationTarget;
import com.wargameclub.clubapi.repository.EventRegistrationRepository;
import com.wargameclub.clubapi.service.ChatRouting;
import com.wargameclub.clubapi.service.NotificationOutboxService;
import com.wargameclub.clubapi.service.TelegramNotificationCommand;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka-консьюмер, планирующий запрос подтверждения участия в мероприятии.
 */
@Service
public class EventAttendanceReminderConsumer {

    /**
     * Ссылочный тип outbox-записей запроса подтверждения участия.
     */
    private static final String ATTENDANCE_REFERENCE = "EVENT_ATTENDANCE_PROMPT";

    /**
     * За сколько часов до старта мероприятия отправлять запрос подтверждения.
     */
    private static final int CONFIRMATION_LEAD_HOURS = 24;

    /**
     * Репозиторий регистраций на мероприятия.
     */
    private final EventRegistrationRepository registrationRepository;

    /**
     * Сервис outbox-уведомлений.
     */
    private final NotificationOutboxService outboxService;

    /**
     * Создает консьюмера запросов подтверждения участия.
     *
     * @param registrationRepository репозиторий регистраций
     * @param outboxService сервис outbox-уведомлений
     */
    public EventAttendanceReminderConsumer(
            EventRegistrationRepository registrationRepository,
            NotificationOutboxService outboxService
    ) {
        this.registrationRepository = registrationRepository;
        this.outboxService = outboxService;
    }

    /**
     * Обрабатывает событие покупки билета: планирует запрос подтверждения участия.
     *
     * @param event событие покупки билета
     * @param acknowledgment подтверждение обработки сообщения
     */
    @KafkaListener(
            topics = KafkaTopics.TICKET_PURCHASED,
            groupId = "event-attendance-reminder",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onTicketPurchased(TicketPurchasedEvent event, Acknowledgment acknowledgment) {
        if (event == null || event.eventId() == null || event.userId() == null) {
            throw new IllegalArgumentException("Некорректные данные события ticket.purchased");
        }
        EventRegistration registration = registrationRepository
                .findByEventIdAndUserId(event.eventId(), event.userId())
                .orElse(null);
        if (registration == null || registration.getId() == null) {
            acknowledgment.acknowledge();
            return;
        }

        outboxService.deletePendingByReference(ATTENDANCE_REFERENCE, registration.getId());

        if (registration.getUser() == null
                || registration.getUser().getTelegramId() == null
                || registration.getEvent() == null
                || registration.getEvent().getStartAt() == null) {
            acknowledgment.acknowledge();
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime reminderAt = registration.getEvent().getStartAt().minusHours(CONFIRMATION_LEAD_HOURS);
        if (reminderAt.isBefore(now)) {
            reminderAt = now;
        }
        String command = TelegramNotificationCommand.eventAttendancePrompt(
                registration.getEvent().getId(),
                registration.getEvent().getTitle()
        );

        outboxService.enqueueAt(
                NotificationTarget.TELEGRAM,
                new ChatRouting(registration.getUser().getTelegramId(), null),
                command,
                reminderAt,
                ATTENDANCE_REFERENCE,
                registration.getId()
        );
        acknowledgment.acknowledge();
    }

    /**
     * Обрабатывает событие отмены билета: удаляет ожидающие запросы подтверждения.
     *
     * @param event событие отмены билета
     * @param acknowledgment подтверждение обработки сообщения
     */
    @KafkaListener(
            topics = KafkaTopics.TICKET_CANCELLED,
            groupId = "event-attendance-reminder",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onTicketCancelled(TicketCancelledEvent event, Acknowledgment acknowledgment) {
        if (event == null || event.eventId() == null || event.userId() == null) {
            throw new IllegalArgumentException("Некорректные данные события ticket.cancelled");
        }
        registrationRepository.findByEventIdAndUserId(event.eventId(), event.userId())
                .map(EventRegistration::getId)
                .ifPresent(id -> outboxService.deletePendingByReference(ATTENDANCE_REFERENCE, id));
        acknowledgment.acknowledge();
    }
}
