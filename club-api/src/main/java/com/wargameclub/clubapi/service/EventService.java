package com.wargameclub.clubapi.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.wargameclub.clubapi.dto.EventUpdateRequest;
import com.wargameclub.clubapi.entity.ClubEvent;
import com.wargameclub.clubapi.entity.EventRegistration;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.enums.EventStatus;
import com.wargameclub.clubapi.enums.EventType;
import com.wargameclub.clubapi.enums.RegistrationStatus;
import com.wargameclub.clubapi.exception.BadRequestException;
import com.wargameclub.clubapi.exception.ConflictException;
import com.wargameclub.clubapi.exception.NotFoundException;
import com.wargameclub.clubapi.messaging.EventUpdatedEvent;
import com.wargameclub.clubapi.messaging.KafkaEventPublisher;
import com.wargameclub.clubapi.messaging.TicketCancelledEvent;
import com.wargameclub.clubapi.messaging.TicketPurchasedEvent;
import com.wargameclub.clubapi.repository.ClubEventRepository;
import com.wargameclub.clubapi.repository.EventRegistrationRepository;
import com.wargameclub.clubapi.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис управления мероприятиями и регистрациями пользователей.
 */
@Service
public class EventService {
    /**
     * Формат даты и времени для уведомлений.
     */
    private static final DateTimeFormatter MESSAGE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm XXX");

    /**
     * Репозиторий мероприятий клуба.
     */
    private final ClubEventRepository eventRepository;

    /**
     * Репозиторий пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Репозиторий регистраций на мероприятия.
     */
    private final EventRegistrationRepository registrationRepository;

    /**
     * Публикатор уведомлений о событиях.
     */
    private final EventPublisher eventPublisher;

    /**
     * Публикатор событий в Kafka.
     */
    private final KafkaEventPublisher kafkaEventPublisher;

    /**
     * Сервис автообновления Telegram-расписания.
     */
    private final TelegramAutoRefreshService autoRefreshService;

    /**
     * Создает сервис мероприятий.
     *
     * @param eventRepository репозиторий мероприятий
     * @param userRepository репозиторий пользователей
     * @param registrationRepository репозиторий регистраций
     * @param eventPublisher публикатор уведомлений
     * @param kafkaEventPublisher публикатор Kafka-событий
     * @param autoRefreshService сервис автообновления
     */
    public EventService(
            ClubEventRepository eventRepository,
            UserRepository userRepository,
            EventRegistrationRepository registrationRepository,
            EventPublisher eventPublisher,
            KafkaEventPublisher kafkaEventPublisher,
            TelegramAutoRefreshService autoRefreshService
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.registrationRepository = registrationRepository;
        this.eventPublisher = eventPublisher;
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.autoRefreshService = autoRefreshService;
    }

    /**
     * Создает мероприятие и публикует уведомление.
     *
     * @param event мероприятие
     * @return созданное мероприятие
     */
    @Transactional
    public ClubEvent create(ClubEvent event) {
        validateRange(event.getStartAt(), event.getEndAt());
        ClubEvent saved = eventRepository.save(event);
        publishNotification(saved, "создано");
        autoRefreshService.refreshEventsIfWithinRange(saved.getStartAt());
        return saved;
    }

    /**
     * Обновляет мероприятие и публикует уведомление и событие в Kafka.
     *
     * @param eventId идентификатор мероприятия
     * @param request запрос на обновление
     * @return обновленное мероприятие
     */
    @Transactional
    public ClubEvent update(Long eventId, EventUpdateRequest request) {
        ClubEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Мероприятие не найдено: " + eventId));
        if (request.title() != null) {
            event.setTitle(request.title());
        }
        if (request.type() != null) {
            event.setType(request.type());
        }
        if (request.description() != null) {
            event.setDescription(request.description());
        }
        if (request.startAt() != null) {
            event.setStartAt(request.startAt());
        }
        if (request.endAt() != null) {
            event.setEndAt(request.endAt());
        }
        if (request.organizerUserId() != null) {
            User organizer = userRepository.findById(request.organizerUserId())
                    .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + request.organizerUserId()));
            event.setOrganizer(organizer);
        }
        if (request.capacity() != null) {
            event.setCapacity(request.capacity());
        }
        if (request.status() != null) {
            event.setStatus(request.status());
        }

        validateRange(event.getStartAt(), event.getEndAt());
        event.setUpdatedAt(OffsetDateTime.now());

        publishNotification(event, "обновлено");
        kafkaEventPublisher.publishEventUpdated(new EventUpdatedEvent(
                event.getId(),
                event.getTitle(),
                event.getType(),
                event.getStatus(),
                event.getStartAt(),
                event.getEndAt(),
                event.getUpdatedAt()
        ));
        autoRefreshService.refreshEvents();
        return event;
    }

    /**
     * Возвращает мероприятия, пересекающие интервал времени.
     *
     * @param from начало интервала
     * @param to конец интервала
     * @param type тип мероприятия (опционально)
     * @return список мероприятий
     */
    @Transactional(readOnly = true)
    public List<ClubEvent> findOverlapping(OffsetDateTime from, OffsetDateTime to, EventType type) {
        validateRange(from, to);
        return eventRepository.findOverlappingWithOrganizer(from, to, type);
    }

    /**
     * Возвращает список уникальных названий мероприятий.
     *
     * @param limit максимальное число названий
     * @return список названий
     */
    @Transactional(readOnly = true)
    public List<String> listTitles(int limit) {
        int size = limit > 0 ? limit : 20;
        return eventRepository.findDistinctTitles(PageRequest.of(0, size));
    }

    /**
     * Регистрирует пользователя на мероприятие и публикует событие покупки билета.
     *
     * @param eventId идентификатор мероприятия
     * @param userId идентификатор пользователя
     * @param count количество билетов (опционально)
     * @param amount сумма оплаты (опционально)
     */
    @Transactional
    public void register(Long eventId, Long userId, Integer count, BigDecimal amount) {
        ClubEvent event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new NotFoundException("Мероприятие не найдено: " + eventId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        if (event.getStatus() != EventStatus.SCHEDULED) {
            throw new BadRequestException("Мероприятие не запланировано");
        }
        if (event.getCapacity() != null) {
            long registered = registrationRepository.countByEventIdAndStatusIn(
                    eventId,
                    List.of(RegistrationStatus.REGISTERED, RegistrationStatus.CONFIRMED)
            );
            if (registered >= event.getCapacity()) {
                throw new ConflictException("Превышена вместимость мероприятия");
            }
        }
        OffsetDateTime now = OffsetDateTime.now();
        EventRegistration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElse(null);
        boolean publishPurchase = false;
        if (registration == null) {
            registration = new EventRegistration(event, user);
            registration.setConfirmationRequestedAt(now);
            registrationRepository.save(registration);
            publishPurchase = true;
        } else if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            registration.setStatus(RegistrationStatus.REGISTERED);
            registration.setConfirmationRequestedAt(now);
            registration.setConfirmedAt(null);
            publishPurchase = true;
        } else if (registration.getStatus() == RegistrationStatus.REGISTERED) {
            registration.setConfirmationRequestedAt(now);
        } else if (registration.getStatus() == RegistrationStatus.CONFIRMED) {
            return;
        }
        if (!publishPurchase) {
            return;
        }
        kafkaEventPublisher.publishTicketPurchased(new TicketPurchasedEvent(
                event.getId(),
                event.getTitle(),
                event.getType(),
                user.getId(),
                user.getName(),
                count == null ? 1 : count,
                amount == null ? BigDecimal.ZERO : amount,
                now
        ));
    }

    /**
     * Отменяет регистрацию пользователя на мероприятие и публикует событие отмены билета.
     *
     * @param eventId идентификатор мероприятия
     * @param userId идентификатор пользователя
     * @param count количество билетов (опционально)
     * @param amount сумма возврата (опционально)
     */
    @Transactional
    public void unregister(Long eventId, Long userId, Integer count, BigDecimal amount) {
        EventRegistration registration = registrationRepository
                .findByEventIdAndUserIdForUpdate(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Регистрация не найдена"));
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            return;
        }
        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setConfirmedAt(null);
        registration.setConfirmationRequestedAt(null);
        OffsetDateTime now = OffsetDateTime.now();
        kafkaEventPublisher.publishTicketCancelled(new TicketCancelledEvent(
                registration.getEvent().getId(),
                registration.getEvent().getTitle(),
                registration.getEvent().getType(),
                registration.getUser().getId(),
                registration.getUser().getName(),
                count == null ? 1 : count,
                amount == null ? BigDecimal.ZERO : amount,
                now
        ));
    }

    /**
     * Подтверждает участие пользователя в мероприятии.
     *
     * @param eventId идентификатор мероприятия
     * @param userId идентификатор пользователя
     */
    @Transactional
    public void confirmAttendance(Long eventId, Long userId) {
        EventRegistration registration = registrationRepository
                .findByEventIdAndUserIdForUpdate(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Регистрация не найдена"));
        if (registration.getEvent().getStatus() != EventStatus.SCHEDULED) {
            throw new BadRequestException("Подтверждение доступно только для запланированного мероприятия");
        }
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new BadRequestException("Нельзя подтвердить отмененную регистрацию");
        }
        if (registration.getStatus() == RegistrationStatus.CONFIRMED) {
            return;
        }
        registration.setStatus(RegistrationStatus.CONFIRMED);
        registration.setConfirmedAt(OffsetDateTime.now());
    }

    /**
     * Отмечает, что пользователь не придет на мероприятие.
     *
     * @param eventId идентификатор мероприятия
     * @param userId идентификатор пользователя
     */
    @Transactional
    public void declineAttendance(Long eventId, Long userId) {
        EventRegistration registration = registrationRepository
                .findByEventIdAndUserIdForUpdate(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Регистрация не найдена"));
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            return;
        }
        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setConfirmedAt(null);
        registration.setConfirmationRequestedAt(null);
        OffsetDateTime now = OffsetDateTime.now();
        kafkaEventPublisher.publishTicketCancelled(new TicketCancelledEvent(
                registration.getEvent().getId(),
                registration.getEvent().getTitle(),
                registration.getEvent().getType(),
                registration.getUser().getId(),
                registration.getUser().getName(),
                1,
                BigDecimal.ZERO,
                now
        ));
    }

    /**
     * Проверяет корректность временного интервала.
     *
     * @param startAt начало интервала
     * @param endAt конец интервала
     */
    private void validateRange(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new BadRequestException("Некорректный диапазон времени");
        }
    }

    /**
     * Публикует уведомление о мероприятии.
     *
     * @param event мероприятие
     * @param action действие (например, "создано", "обновлено")
     */
    private void publishNotification(ClubEvent event, String action) {
        String message = "Мероприятие " + action + ": " + event.getTitle()
                + " (" + formatEventType(event.getType()) + ")\n"
                + "Организатор: " + event.getOrganizer().getName() + "\n"
                + "Когда: " + event.getStartAt().format(MESSAGE_FORMAT) + " - " + event.getEndAt().format(MESSAGE_FORMAT)
                + "\nСтатус: " + formatEventStatus(event.getStatus());
        eventPublisher.publishEventNotification(message);
    }

    /**
     * Форматирует тип мероприятия в человекочитаемый вид.
     *
     * @param type тип мероприятия
     * @return строковое представление
     */
    private String formatEventType(EventType type) {
        if (type == null) {
            return "-";
        }
        return switch (type) {
            case PAINT_DAY -> "День покраски";
            case WORK_DAY -> "Рабочий день";
            case TOURNAMENT -> "Турнир";
            case OTHER -> "Другое";
        };
    }

    /**
     * Форматирует статус мероприятия в человекочитаемый вид.
     *
     * @param status статус мероприятия
     * @return строковое представление
     */
    private String formatEventStatus(EventStatus status) {
        if (status == null) {
            return "-";
        }
        return switch (status) {
            case SCHEDULED -> "Запланировано";
            case CANCELLED -> "Отменено";
        };
    }
}
