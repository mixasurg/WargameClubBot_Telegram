package com.wargameclub.clubapi.messaging;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import com.wargameclub.clubapi.entity.ClubEvent;
import com.wargameclub.clubapi.entity.EventRegistration;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.enums.EventType;
import com.wargameclub.clubapi.enums.NotificationTarget;
import com.wargameclub.clubapi.repository.EventRegistrationRepository;
import com.wargameclub.clubapi.service.ChatRouting;
import com.wargameclub.clubapi.service.NotificationOutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventAttendanceReminderConsumerTest {

    @Mock
    private EventRegistrationRepository registrationRepository;
    @Mock
    private NotificationOutboxService outboxService;
    @Mock
    private Acknowledgment acknowledgment;

    private EventAttendanceReminderConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new EventAttendanceReminderConsumer(registrationRepository, outboxService);
    }

    @Test
    void onTicketPurchasedSchedulesAttendancePrompt() {
        User user = new User("User");
        user.setTelegramId(100L);

        OffsetDateTime startAt = OffsetDateTime.now().plusDays(2);
        ClubEvent clubEvent = new ClubEvent();
        ReflectionTestUtils.setField(clubEvent, "id", 5L);
        clubEvent.setTitle("Турнир");
        clubEvent.setStartAt(startAt);
        clubEvent.setEndAt(startAt.plusHours(4));

        EventRegistration registration = new EventRegistration(clubEvent, user);
        ReflectionTestUtils.setField(registration, "id", 9L);

        when(registrationRepository.findByEventIdAndUserId(5L, 77L)).thenReturn(Optional.of(registration));

        TicketPurchasedEvent event = new TicketPurchasedEvent(
                5L,
                "Турнир",
                EventType.TOURNAMENT,
                77L,
                "User",
                1,
                BigDecimal.ZERO,
                OffsetDateTime.now()
        );

        consumer.onTicketPurchased(event, acknowledgment);

        verify(outboxService).deletePendingByReference("EVENT_ATTENDANCE_PROMPT", 9L);

        ArgumentCaptor<ChatRouting> routingCaptor = ArgumentCaptor.forClass(ChatRouting.class);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<OffsetDateTime> timeCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        verify(outboxService).enqueueAt(
                eq(NotificationTarget.TELEGRAM),
                routingCaptor.capture(),
                textCaptor.capture(),
                timeCaptor.capture(),
                eq("EVENT_ATTENDANCE_PROMPT"),
                eq(9L)
        );

        assertThat(routingCaptor.getValue().chatId()).isEqualTo(100L);
        assertThat(textCaptor.getValue()).isEqualTo("__cmd:event_attendance_prompt__:5:Турнир");
        assertThat(timeCaptor.getValue()).isEqualTo(startAt.minusHours(24));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void onTicketPurchasedWithoutTelegramSkipsEnqueue() {
        User user = new User("User");

        OffsetDateTime startAt = OffsetDateTime.now().plusDays(2);
        ClubEvent clubEvent = new ClubEvent();
        ReflectionTestUtils.setField(clubEvent, "id", 8L);
        clubEvent.setTitle("Покрас");
        clubEvent.setStartAt(startAt);
        clubEvent.setEndAt(startAt.plusHours(2));

        EventRegistration registration = new EventRegistration(clubEvent, user);
        ReflectionTestUtils.setField(registration, "id", 12L);

        when(registrationRepository.findByEventIdAndUserId(8L, 88L)).thenReturn(Optional.of(registration));

        TicketPurchasedEvent event = new TicketPurchasedEvent(
                8L,
                "Покрас",
                EventType.PAINT_DAY,
                88L,
                "User",
                1,
                BigDecimal.ZERO,
                OffsetDateTime.now()
        );

        consumer.onTicketPurchased(event, acknowledgment);

        verify(outboxService).deletePendingByReference("EVENT_ATTENDANCE_PROMPT", 12L);
        verify(outboxService, never()).enqueueAt(
                any(NotificationTarget.class),
                any(ChatRouting.class),
                anyString(),
                any(OffsetDateTime.class),
                anyString(),
                any(Long.class)
        );
        verify(acknowledgment).acknowledge();
    }

    @Test
    void onTicketCancelledDeletesPendingPrompt() {
        ClubEvent clubEvent = new ClubEvent();
        ReflectionTestUtils.setField(clubEvent, "id", 17L);

        User user = new User("User");
        EventRegistration registration = new EventRegistration(clubEvent, user);
        ReflectionTestUtils.setField(registration, "id", 23L);

        when(registrationRepository.findByEventIdAndUserId(17L, 70L)).thenReturn(Optional.of(registration));

        TicketCancelledEvent event = new TicketCancelledEvent(
                17L,
                "Турнир",
                EventType.TOURNAMENT,
                70L,
                "User",
                1,
                BigDecimal.ZERO,
                OffsetDateTime.now()
        );

        consumer.onTicketCancelled(event, acknowledgment);

        verify(outboxService).deletePendingByReference("EVENT_ATTENDANCE_PROMPT", 23L);
        verify(acknowledgment).acknowledge();
    }
}
