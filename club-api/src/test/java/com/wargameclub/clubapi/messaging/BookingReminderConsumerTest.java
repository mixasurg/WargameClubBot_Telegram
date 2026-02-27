package com.wargameclub.clubapi.messaging;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import com.wargameclub.clubapi.config.AppProperties;
import com.wargameclub.clubapi.entity.Booking;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.enums.BookingStatus;
import com.wargameclub.clubapi.enums.NotificationTarget;
import com.wargameclub.clubapi.repository.BookingRepository;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingReminderConsumerTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private NotificationOutboxService outboxService;
    @Mock
    private Acknowledgment acknowledgment;

    private BookingReminderConsumer consumer;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties();
        props.setTimezone(ZoneId.of("UTC"));
        consumer = new BookingReminderConsumer(bookingRepository, outboxService, props);
    }

    @Test
    void onBookingCreatedSchedulesRemindersAndResultPrompts() {
        User user = new User("User");
        user.setTelegramId(100L);
        User opponent = new User("Opponent");
        opponent.setTelegramId(200L);
        Booking booking = new Booking(new ClubTable("T1", true, null), user,
                OffsetDateTime.now().plusDays(2), OffsetDateTime.now().plusDays(2).plusHours(2));
        booking.setStatus(BookingStatus.CREATED);
        booking.setOpponent(opponent);
        booking.setGame("Game");
        ReflectionTestUtils.setField(booking, "id", 5L);

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        consumer.onBookingCreated(new BookingCreatedEvent(5L), acknowledgment);

        verify(outboxService).deletePendingByReference("BOOKING_REMINDER", 5L);
        verify(outboxService).deletePendingByReference("BOOKING_RESULT_PROMPT", 5L);
        ArgumentCaptor<String> refTypeCaptor = ArgumentCaptor.forClass(String.class);
        verify(outboxService, times(4)).enqueueAt(
                eq(NotificationTarget.TELEGRAM),
                any(ChatRouting.class),
                anyString(),
                any(OffsetDateTime.class),
                refTypeCaptor.capture(),
                eq(5L)
        );
        List<String> refs = refTypeCaptor.getAllValues();
        assertThat(refs).containsExactlyInAnyOrder(
                "BOOKING_REMINDER",
                "BOOKING_REMINDER",
                "BOOKING_RESULT_PROMPT",
                "BOOKING_RESULT_PROMPT"
        );
        verify(acknowledgment).acknowledge();
    }

    @Test
    void onBookingCancelledDeletesPendingNotifications() {
        consumer.onBookingCancelled(new BookingCancelledEvent(7L), acknowledgment);

        verify(outboxService).deletePendingByReference("BOOKING_REMINDER", 7L);
        verify(outboxService).deletePendingByReference("BOOKING_RESULT_PROMPT", 7L);
        verify(acknowledgment).acknowledge();
    }
}
