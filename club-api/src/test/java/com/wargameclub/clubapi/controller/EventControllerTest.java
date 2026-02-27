package com.wargameclub.clubapi.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubapi.dto.EventCreateRequest;
import com.wargameclub.clubapi.dto.EventDto;
import com.wargameclub.clubapi.dto.EventRegistrationRequest;
import com.wargameclub.clubapi.dto.EventUpdateRequest;
import com.wargameclub.clubapi.entity.ClubEvent;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.enums.EventStatus;
import com.wargameclub.clubapi.enums.EventType;
import com.wargameclub.clubapi.service.EventService;
import com.wargameclub.clubapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;
    @Mock
    private UserService userService;

    private EventController controller;

    @BeforeEach
    void setUp() {
        controller = new EventController(eventService, userService);
    }

    @Test
    void createBuildsEventAndReturnsDto() {
        User organizer = new User("Owner");
        ReflectionTestUtils.setField(organizer, "id", 10L);
        when(userService.getById(10L)).thenReturn(organizer);

        ClubEvent created = new ClubEvent();
        ReflectionTestUtils.setField(created, "id", 5L);
        created.setTitle("Event");
        created.setType(EventType.OTHER);
        created.setOrganizer(organizer);
        created.setStartAt(OffsetDateTime.now().plusDays(1));
        created.setEndAt(created.getStartAt().plusHours(2));
        when(eventService.create(org.mockito.ArgumentMatchers.any(ClubEvent.class))).thenReturn(created);

        EventCreateRequest request = new EventCreateRequest(
                "Event",
                EventType.OTHER,
                "Desc",
                created.getStartAt(),
                created.getEndAt(),
                10L,
                20
        );

        EventDto dto = controller.create(request);

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.organizerUserId()).isEqualTo(10L);

        ArgumentCaptor<ClubEvent> captor = ArgumentCaptor.forClass(ClubEvent.class);
        verify(eventService).create(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Event");
        assertThat(captor.getValue().getOrganizer()).isSameAs(organizer);
    }

    @Test
    void updateDelegatesToService() {
        ClubEvent updated = new ClubEvent();
        ReflectionTestUtils.setField(updated, "id", 1L);
        updated.setTitle("Updated");
        updated.setType(EventType.TOURNAMENT);
        updated.setOrganizer(new User("Owner"));
        updated.setStartAt(OffsetDateTime.now().plusDays(1));
        updated.setEndAt(updated.getStartAt().plusHours(2));
        when(eventService.update(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(EventUpdateRequest.class)))
                .thenReturn(updated);

        EventDto dto = controller.update(1L, new EventUpdateRequest(
                "Updated",
                EventType.TOURNAMENT,
                null,
                updated.getStartAt(),
                updated.getEndAt(),
                null,
                null,
                EventStatus.SCHEDULED
        ));

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.title()).isEqualTo("Updated");
    }

    @Test
    void listDelegatesToService() {
        when(eventService.findOverlapping(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        List<EventDto> result = controller.list(OffsetDateTime.now(), OffsetDateTime.now().plusDays(1), null);

        assertThat(result).isEmpty();
    }

    @Test
    void registerDelegatesToService() {
        EventRegistrationRequest request = new EventRegistrationRequest(10L, 2, new BigDecimal("100"));

        controller.register(5L, request);

        verify(eventService).register(5L, 10L, 2, new BigDecimal("100"));
    }

    @Test
    void unregisterDelegatesToService() {
        EventRegistrationRequest request = new EventRegistrationRequest(10L, 1, BigDecimal.ZERO);

        controller.unregister(5L, request);

        verify(eventService).unregister(5L, 10L, 1, BigDecimal.ZERO);
    }
}
