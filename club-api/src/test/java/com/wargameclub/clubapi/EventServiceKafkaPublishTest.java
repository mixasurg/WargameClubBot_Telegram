package com.wargameclub.clubapi;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.wargameclub.clubapi.dto.EventUpdateRequest;
import com.wargameclub.clubapi.entity.ClubEvent;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.enums.EventStatus;
import com.wargameclub.clubapi.enums.EventType;
import com.wargameclub.clubapi.messaging.EventUpdatedEvent;
import com.wargameclub.clubapi.messaging.KafkaEventPublisher;
import com.wargameclub.clubapi.messaging.TicketCancelledEvent;
import com.wargameclub.clubapi.messaging.TicketPurchasedEvent;
import com.wargameclub.clubapi.repository.ClubEventRepository;
import com.wargameclub.clubapi.repository.UserRepository;
import com.wargameclub.clubapi.service.EventPublisher;
import com.wargameclub.clubapi.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

/**
 * Класс модуля club-api.
 */
@SpringBootTest
@ActiveProfiles("test")
public class EventServiceKafkaPublishTest {

    /**
     * Сервис мероприятия.
     */
    @Autowired
    private EventService eventService;

    /**
     * Репозиторий мероприятия клуба.
     */
    @Autowired
    private ClubEventRepository eventRepository;

    /**
     * Репозиторий пользователя.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Поле состояния.
     */
    @MockBean
    private KafkaEventPublisher kafkaEventPublisher;

    /**
     * Поле состояния.
     */
    @MockBean
    private EventPublisher eventPublisher;

    /**
     * Выполняет операцию.
     */
    @BeforeEach
    void resetMocks() {
        Mockito.reset(kafkaEventPublisher, eventPublisher);
    }

    /**
     * Регистрирует PublishesTicketPurchased.
     */
    @Test
    void registerPublishesTicketPurchased() {
        ClubEvent event = createEvent();
        User user = userRepository.save(new User("Buyer"));

        eventService.register(event.getId(), user.getId(), 2, new BigDecimal("1000"));

        ArgumentCaptor<TicketPurchasedEvent> captor = ArgumentCaptor.forClass(TicketPurchasedEvent.class);

        /**
         * Выполняет операцию.
         */
        verify(kafkaEventPublisher).publishTicketPurchased(captor.capture());
        TicketPurchasedEvent payload = captor.getValue();

        /**
         * Выполняет операцию.
         */
        assertEquals(event.getId(), payload.eventId());

        /**
         * Выполняет операцию.
         */
        assertEquals(user.getId(), payload.userId());

        /**
         * Выполняет операцию.
         */
        assertEquals(2, payload.count());

        /**
         * Выполняет операцию.
         */
        assertEquals(new BigDecimal("1000"), payload.amount());
    }

    /**
     * Выполняет операцию.
     */
    @Test
    void unregisterPublishesTicketCancelled() {
        ClubEvent event = createEvent();
        User user = userRepository.save(new User("Buyer"));

        eventService.register(event.getId(), user.getId(), 1, new BigDecimal("500"));
        Mockito.reset(kafkaEventPublisher, eventPublisher);

        eventService.unregister(event.getId(), user.getId(), 1, new BigDecimal("500"));

        ArgumentCaptor<TicketCancelledEvent> captor = ArgumentCaptor.forClass(TicketCancelledEvent.class);

        /**
         * Выполняет операцию.
         */
        verify(kafkaEventPublisher).publishTicketCancelled(captor.capture());
        TicketCancelledEvent payload = captor.getValue();

        /**
         * Выполняет операцию.
         */
        assertEquals(event.getId(), payload.eventId());

        /**
         * Выполняет операцию.
         */
        assertEquals(user.getId(), payload.userId());

        /**
         * Выполняет операцию.
         */
        assertEquals(1, payload.count());

        /**
         * Выполняет операцию.
         */
        assertEquals(new BigDecimal("500"), payload.amount());
    }

    /**
     * Обновляет PublishesEventUpdated.
     */
    @Test
    void updatePublishesEventUpdated() {
        ClubEvent event = createEvent();
        OffsetDateTime start = event.getStartAt().plusDays(1);
        OffsetDateTime end = start.plusHours(2);

        /**
         * Выполняет операцию.
         */
        EventUpdateRequest request = new EventUpdateRequest(
                "New title",
                EventType.OTHER,
                "Updated",
                start,
                end,
                event.getOrganizer().getId(),
                10,
                EventStatus.SCHEDULED
        );

        eventService.update(event.getId(), request);

        ArgumentCaptor<EventUpdatedEvent> captor = ArgumentCaptor.forClass(EventUpdatedEvent.class);

        /**
         * Выполняет операцию.
         */
        verify(kafkaEventPublisher).publishEventUpdated(captor.capture());
        EventUpdatedEvent payload = captor.getValue();

        /**
         * Выполняет операцию.
         */
        assertEquals(event.getId(), payload.eventId());

        /**
         * Выполняет операцию.
         */
        assertEquals("New title", payload.title());

        /**
         * Выполняет операцию.
         */
        assertNotNull(payload.updatedAt());
    }

    /**
     * Создает мероприятие.
     */
    private ClubEvent createEvent() {
        User organizer = userRepository.save(new User("Organizer"));
        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        OffsetDateTime end = start.plusHours(2);

        ClubEvent event = new ClubEvent();
        event.setTitle("Test event");
        event.setType(EventType.OTHER);
        event.setDescription("Desc");
        event.setStartAt(start);
        event.setEndAt(end);
        event.setOrganizer(organizer);
        event.setStatus(EventStatus.SCHEDULED);
        return eventRepository.save(event);
    }
}
