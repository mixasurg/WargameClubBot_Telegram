package com.wargameclub.clubapi.messaging;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import com.wargameclub.clubapi.config.AppProperties;
import com.wargameclub.clubapi.entity.Booking;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.enums.BookingStatus;
import com.wargameclub.clubapi.enums.NotificationTarget;
import com.wargameclub.clubapi.repository.BookingRepository;
import com.wargameclub.clubapi.service.ChatRouting;
import com.wargameclub.clubapi.service.NotificationOutboxService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka-консьюмер, планирующий напоминания о бронированиях и запрос результата игры.
 */
@Service
public class BookingReminderConsumer {

    /**
     * Тип ссылки для напоминаний о бронировании.
     */
    private static final String REMINDER_REFERENCE = "BOOKING_REMINDER";

    /**
     * Тип ссылки для запроса результата игры.
     */
    private static final String RESULT_REFERENCE = "BOOKING_RESULT_PROMPT";

    /**
     * Команда для запроса результата игры.
     */
    private static final String RESULT_PROMPT_COMMAND = "__cmd:result_prompt__";

    /**
     * Задержка запроса результата после окончания бронирования (минуты).
     */
    private static final int RESULT_DELAY_MINUTES = 0;
    /**
     * Формат даты и времени в напоминании.
     */
    private static final DateTimeFormatter REMINDER_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Репозиторий бронирований.
     */
    private final BookingRepository bookingRepository;

    /**
     * Сервис outbox-уведомлений.
     */
    private final NotificationOutboxService outboxService;

    /**
     * Настройки приложения.
     */
    private final AppProperties appProperties;

    /**
     * Создает консьюмера напоминаний о бронированиях.
     *
     * @param bookingRepository репозиторий бронирований
     * @param outboxService сервис outbox-уведомлений
     * @param appProperties параметры приложения
     */
    public BookingReminderConsumer(
            BookingRepository bookingRepository,
            NotificationOutboxService outboxService,
            AppProperties appProperties
    ) {
        this.bookingRepository = bookingRepository;
        this.outboxService = outboxService;
        this.appProperties = appProperties;
    }

    /**
     * Обрабатывает событие создания бронирования: планирует напоминание и запрос результата.
     *
     * @param event событие создания бронирования
     * @param acknowledgment подтверждение обработки сообщения
     */
    @KafkaListener(
            topics = KafkaTopics.BOOKING_CREATED,
            groupId = "booking-reminder",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onBookingCreated(BookingCreatedEvent event, Acknowledgment acknowledgment) {
        if (event == null || event.bookingId() == null) {
            throw new IllegalArgumentException("Некорректные данные события booking.created");
        }
        Booking booking = bookingRepository.findById(event.bookingId())
                .orElseThrow(() -> new IllegalArgumentException("Бронирование не найдено: " + event.bookingId()));
        if (booking.getStatus() != BookingStatus.CREATED) {
            acknowledgment.acknowledge();
            return;
        }
        OffsetDateTime startAt = booking.getStartAt();
        if (startAt == null) {
            throw new IllegalArgumentException("У бронирования отсутствует startAt");
        }
        OffsetDateTime now = OffsetDateTime.now();
        outboxService.deletePendingByReference(REMINDER_REFERENCE, booking.getId());
        outboxService.deletePendingByReference(RESULT_REFERENCE, booking.getId());
        ZoneId zoneId = appProperties.getTimezone();
        String message = buildReminderMessage(booking, zoneId);

        if (!startAt.isBefore(now)) {
            OffsetDateTime reminderAt = startAt.minusDays(1);
            if (reminderAt.isBefore(now)) {
                reminderAt = now;
            }
            enqueueReminder(booking.getUser(), message, reminderAt, booking.getId());
            if (booking.getOpponent() != null) {
                enqueueReminder(booking.getOpponent(), message, reminderAt, booking.getId());
            }
        }
        if (booking.getOpponent() != null && booking.getEndAt() != null) {
            OffsetDateTime resultAt = booking.getEndAt().plusMinutes(RESULT_DELAY_MINUTES);
            if (resultAt.isBefore(now)) {
                resultAt = now;
            }
            String command = RESULT_PROMPT_COMMAND + ":" + booking.getId();
            enqueueResultPrompt(booking.getUser(), command, resultAt, booking.getId());
            enqueueResultPrompt(booking.getOpponent(), command, resultAt, booking.getId());
        }
        acknowledgment.acknowledge();
    }

    /**
     * Обрабатывает событие отмены бронирования: удаляет запланированные уведомления.
     *
     * @param event событие отмены бронирования
     * @param acknowledgment подтверждение обработки сообщения
     */
    @KafkaListener(
            topics = KafkaTopics.BOOKING_CANCELLED,
            groupId = "booking-reminder",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void onBookingCancelled(BookingCancelledEvent event, Acknowledgment acknowledgment) {
        if (event == null || event.bookingId() == null) {
            throw new IllegalArgumentException("Некорректные данные события booking.cancelled");
        }
        outboxService.deletePendingByReference(REMINDER_REFERENCE, event.bookingId());
        outboxService.deletePendingByReference(RESULT_REFERENCE, event.bookingId());
        acknowledgment.acknowledge();
    }

    /**
     * Ставит в очередь напоминание пользователю.
     *
     * @param user пользователь
     * @param text текст напоминания
     * @param reminderAt время отправки напоминания
     * @param bookingId идентификатор бронирования
     */
    private void enqueueReminder(User user, String text, OffsetDateTime reminderAt, Long bookingId) {
        if (user == null || user.getTelegramId() == null) {
            return;
        }
        ChatRouting routing = new ChatRouting(user.getTelegramId(), null);
        outboxService.enqueueAt(
                NotificationTarget.TELEGRAM,
                routing,
                text,
                reminderAt,
                REMINDER_REFERENCE,
                bookingId
        );
    }

    /**
     * Ставит в очередь запрос результата игры пользователю.
     *
     * @param user пользователь
     * @param text команда запроса результата
     * @param resultAt время отправки запроса
     * @param bookingId идентификатор бронирования
     */
    private void enqueueResultPrompt(User user, String text, OffsetDateTime resultAt, Long bookingId) {
        if (user == null || user.getTelegramId() == null) {
            return;
        }
        ChatRouting routing = new ChatRouting(user.getTelegramId(), null);
        outboxService.enqueueAt(
                NotificationTarget.TELEGRAM,
                routing,
                text,
                resultAt,
                RESULT_REFERENCE,
                bookingId
        );
    }

    /**
     * Формирует текст напоминания для бронирования.
     *
     * @param booking бронирование
     * @param zoneId часовой пояс
     * @return текст напоминания
     */
    private String buildReminderMessage(Booking booking, ZoneId zoneId) {
        String game = booking.getGame() != null ? booking.getGame() : "-";
        String opponent = booking.getOpponent() != null ? booking.getOpponent().getName() : null;
        String when = booking.getStartAt()
                .atZoneSameInstant(zoneId)
                .format(REMINDER_FORMAT);
        StringBuilder text = new StringBuilder("Напоминание о предстоящей игре:\n");
        text.append("Игра: ").append(game).append("\n");
        text.append("Когда: ").append(when);
        if (opponent != null && !opponent.isBlank()) {
            text.append("\nСоперник: ").append(opponent);
        }
        return text.toString();
    }
}
