package com.wargameclub.clubapi.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.wargameclub.clubapi.dto.EventUpdateRequest;
import com.wargameclub.clubapi.entity.ClubEvent;
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

@Service
public class EventService {
    private static final DateTimeFormatter MESSAGE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm XXX");

    private final ClubEventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventRegistrationRepository registrationRepository;
    private final EventPublisher eventPublisher;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final TelegramAutoRefreshService autoRefreshService;

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

    @Transactional
    public ClubEvent create(ClubEvent event) {
        validateRange(event.getStartAt(), event.getEndAt());
        ClubEvent saved = eventRepository.save(event);
        publishNotification(saved, "создано");
        autoRefreshService.refreshEventsIfWithinRange(saved.getStartAt());
        return saved;
    }

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

    @Transactional(readOnly = true)
    public List<ClubEvent> findOverlapping(OffsetDateTime from, OffsetDateTime to, EventType type) {
        validateRange(from, to);
        return eventRepository.findOverlappingWithOrganizer(from, to, type);
    }

    @Transactional(readOnly = true)
    public List<String> listTitles(int limit) {
        int size = limit > 0 ? limit : 20;
        return eventRepository.findDistinctTitles(PageRequest.of(0, size));
    }

    @Transactional
    public void register(Long eventId, Long userId, Integer count, BigDecimal amount) {
        ClubEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Мероприятие не найдено: " + eventId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        if (event.getStatus() != EventStatus.SCHEDULED) {
            throw new BadRequestException("Мероприятие не запланировано");
        }
        if (event.getCapacity() != null) {
            long registered = registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.REGISTERED);
            if (registered >= event.getCapacity()) {
                throw new ConflictException("Превышена вместимость мероприятия");
            }
        }
        registrationRepository.findByEventIdAndUserId(eventId, userId)
                .ifPresentOrElse(existing -> {
                    existing.setStatus(RegistrationStatus.REGISTERED);
                }, () -> registrationRepository.save(new com.wargameclub.clubapi.entity.EventRegistration(event, user)));
        kafkaEventPublisher.publishTicketPurchased(new TicketPurchasedEvent(
                event.getId(),
                event.getTitle(),
                event.getType(),
                user.getId(),
                user.getName(),
                count == null ? 1 : count,
                amount == null ? BigDecimal.ZERO : amount,
                OffsetDateTime.now()
        ));
    }

    @Transactional
    public void unregister(Long eventId, Long userId, Integer count, BigDecimal amount) {
        com.wargameclub.clubapi.entity.EventRegistration registration = registrationRepository
                .findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Регистрация не найдена"));
        registration.setStatus(RegistrationStatus.CANCELLED);
        kafkaEventPublisher.publishTicketCancelled(new TicketCancelledEvent(
                registration.getEvent().getId(),
                registration.getEvent().getTitle(),
                registration.getEvent().getType(),
                registration.getUser().getId(),
                registration.getUser().getName(),
                count == null ? 1 : count,
                amount == null ? BigDecimal.ZERO : amount,
                OffsetDateTime.now()
        ));
    }

    private void validateRange(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new BadRequestException("Некорректный диапазон времени");
        }
    }

    private void publishNotification(ClubEvent event, String action) {
        String message = "Мероприятие " + action + ": " + event.getTitle()
                + " (" + formatEventType(event.getType()) + ")\n"
                + "Организатор: " + event.getOrganizer().getName() + "\n"
                + "Когда: " + event.getStartAt().format(MESSAGE_FORMAT) + " - " + event.getEndAt().format(MESSAGE_FORMAT)
                + "\nСтатус: " + formatEventStatus(event.getStatus());
        eventPublisher.publishEventNotification(message);
    }

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

