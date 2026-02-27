package com.wargameclub.clubapi.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private ClubEventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRegistrationRepository registrationRepository;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private KafkaEventPublisher kafkaEventPublisher;
    @Mock
    private TelegramAutoRefreshService autoRefreshService;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(
                eventRepository,
                userRepository,
                registrationRepository,
                eventPublisher,
                kafkaEventPublisher,
                autoRefreshService
        );
    }

    @Test
    void createPublishesNotificationAndRefreshes() {
        ClubEvent event = new ClubEvent();
        event.setTitle("Paint Day");
        event.setType(EventType.PAINT_DAY);
        event.setOrganizer(new User("Owner"));
        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        event.setStartAt(start);
        event.setEndAt(start.plusHours(2));

        when(eventRepository.save(event)).thenReturn(event);

        ClubEvent saved = eventService.create(event);

        assertThat(saved).isSameAs(event);
        verify(eventPublisher).publishEventNotification(any(String.class));
        verify(autoRefreshService).refreshEventsIfWithinRange(start);
    }

    @Test
    void updateChangesFieldsAndPublishesEvent() {
        ClubEvent event = new ClubEvent();
        ReflectionTestUtils.setField(event, "id", 1L);
        event.setTitle("Old");
        event.setType(EventType.OTHER);
        event.setOrganizer(new User("OldOwner"));
        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        event.setStartAt(start);
        event.setEndAt(start.plusHours(2));
        OffsetDateTime oldUpdatedAt = event.getUpdatedAt();

        User newOrganizer = new User("NewOwner");
        ReflectionTestUtils.setField(newOrganizer, "id", 99L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(99L)).thenReturn(Optional.of(newOrganizer));

        EventUpdateRequest request = new EventUpdateRequest(
                "New",
                EventType.TOURNAMENT,
                "Desc",
                start.plusDays(1),
                start.plusDays(1).plusHours(2),
                99L,
                10,
                EventStatus.SCHEDULED
        );

        ClubEvent updated = eventService.update(1L, request);

        assertThat(updated.getTitle()).isEqualTo("New");
        assertThat(updated.getType()).isEqualTo(EventType.TOURNAMENT);
        assertThat(updated.getOrganizer()).isSameAs(newOrganizer);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(oldUpdatedAt);
        verify(eventPublisher).publishEventNotification(any(String.class));
        verify(autoRefreshService).refreshEvents();
        ArgumentCaptor<EventUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(EventUpdatedEvent.class);
        verify(kafkaEventPublisher).publishEventUpdated(eventCaptor.capture());
        assertThat(eventCaptor.getValue().eventId()).isEqualTo(1L);
    }

    @Test
    void registerRejectsWhenCapacityExceeded() {
        ClubEvent event = new ClubEvent();
        ReflectionTestUtils.setField(event, "id", 5L);
        event.setTitle("Event");
        event.setType(EventType.OTHER);
        event.setStatus(EventStatus.SCHEDULED);
        event.setCapacity(1);

        when(eventRepository.findById(5L)).thenReturn(Optional.of(event));
        when(userRepository.findById(10L)).thenReturn(Optional.of(new User("Bob")));
        when(registrationRepository.countByEventIdAndStatus(5L, RegistrationStatus.REGISTERED)).thenReturn(1L);

        assertThatThrownBy(() -> eventService.register(5L, 10L, null, null))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void registerUpdatesExistingRegistrationAndPublishesTicket() {
        ClubEvent event = new ClubEvent();
        ReflectionTestUtils.setField(event, "id", 7L);
        event.setTitle("Event");
        event.setType(EventType.OTHER);
        event.setStatus(EventStatus.SCHEDULED);

        User user = new User("Bob");
        ReflectionTestUtils.setField(user, "id", 10L);
        EventRegistration registration = new EventRegistration(event, user);
        registration.setStatus(RegistrationStatus.CANCELLED);

        when(eventRepository.findById(7L)).thenReturn(Optional.of(event));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(registrationRepository.findByEventIdAndUserId(7L, 10L)).thenReturn(Optional.of(registration));

        eventService.register(7L, 10L, null, null);

        assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.REGISTERED);
        ArgumentCaptor<TicketPurchasedEvent> eventCaptor = ArgumentCaptor.forClass(TicketPurchasedEvent.class);
        verify(kafkaEventPublisher).publishTicketPurchased(eventCaptor.capture());
        assertThat(eventCaptor.getValue().count()).isEqualTo(1);
        assertThat(eventCaptor.getValue().amount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void unregisterMarksCancelledAndPublishesTicket() {
        ClubEvent event = new ClubEvent();
        ReflectionTestUtils.setField(event, "id", 7L);
        event.setTitle("Event");
        event.setType(EventType.OTHER);
        User user = new User("Bob");
        ReflectionTestUtils.setField(user, "id", 10L);
        EventRegistration registration = new EventRegistration(event, user);

        when(registrationRepository.findByEventIdAndUserId(7L, 10L)).thenReturn(Optional.of(registration));

        eventService.unregister(7L, 10L, 2, new BigDecimal("100"));

        assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.CANCELLED);
        ArgumentCaptor<TicketCancelledEvent> eventCaptor = ArgumentCaptor.forClass(TicketCancelledEvent.class);
        verify(kafkaEventPublisher).publishTicketCancelled(eventCaptor.capture());
        assertThat(eventCaptor.getValue().count()).isEqualTo(2);
        assertThat(eventCaptor.getValue().amount()).isEqualTo(new BigDecimal("100"));
    }

    @Test
    void findOverlappingValidatesRange() {
        assertThatThrownBy(() -> eventService.findOverlapping(null, OffsetDateTime.now(), null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void listTitlesUsesDefaultLimit() {
        when(eventRepository.findDistinctTitles(PageRequest.of(0, 20))).thenReturn(List.of("A", "B"));

        List<String> titles = eventService.listTitles(0);

        assertThat(titles).containsExactly("A", "B");
    }
}
