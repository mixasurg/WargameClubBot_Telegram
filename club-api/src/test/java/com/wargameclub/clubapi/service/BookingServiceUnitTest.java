package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wargameclub.clubapi.dto.BookingCreateRequest;
import com.wargameclub.clubapi.entity.Army;
import com.wargameclub.clubapi.entity.Booking;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.enums.BookingStatus;
import com.wargameclub.clubapi.exception.BadRequestException;
import com.wargameclub.clubapi.exception.ConflictException;
import com.wargameclub.clubapi.messaging.BookingCancelledEvent;
import com.wargameclub.clubapi.messaging.BookingCreatedEvent;
import com.wargameclub.clubapi.messaging.KafkaEventPublisher;
import com.wargameclub.clubapi.repository.ArmyRepository;
import com.wargameclub.clubapi.repository.BookingRepository;
import com.wargameclub.clubapi.repository.ClubTableRepository;
import com.wargameclub.clubapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceUnitTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ClubTableRepository tableRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ArmyRepository armyRepository;
    @Mock
    private TelegramAutoRefreshService autoRefreshService;
    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    private ObjectMapper objectMapper;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        bookingService = new BookingService(
                bookingRepository,
                tableRepository,
                userRepository,
                armyRepository,
                objectMapper,
                autoRefreshService,
                kafkaEventPublisher
        );
    }

    @Test
    void createAllocatesPreferredTableAndPublishesEvent() throws Exception {
        ClubTable table1 = new ClubTable("T1", true, null);
        ReflectionTestUtils.setField(table1, "id", 1L);
        ClubTable table2 = new ClubTable("T2", true, null);
        ReflectionTestUtils.setField(table2, "id", 2L);
        User user = new User("Alice");
        ReflectionTestUtils.setField(user, "id", 10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(tableRepository.findAll()).thenReturn(List.of(table1, table2));
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table1));
        when(bookingRepository.findOverlappingWithDetails(eq(BookingStatus.CREATED), any(), any()))
                .thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            ReflectionTestUtils.setField(booking, "id", 100L);
            return booking;
        });

        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        OffsetDateTime end = start.plusHours(2);
        BookingCreateRequest request = new BookingCreateRequest(
                table1.getId(),
                user.getId(),
                start,
                end,
                "Game",
                2,
                null,
                null,
                null
        );

        Booking saved = bookingService.create(request);

        assertThat(saved.getTable()).isSameAs(table1);
        List<Map<String, Object>> allocations = objectMapper.readValue(
                saved.getTableAssignments(),
                new TypeReference<>() {
                }
        );
        assertThat(allocations).hasSize(1);
        assertThat(((Number) allocations.get(0).get("tableId")).longValue()).isEqualTo(1L);
        assertThat(((Number) allocations.get(0).get("units")).intValue()).isEqualTo(2);

        verify(autoRefreshService).refreshTwoweeksIfWithinRange(start);
        ArgumentCaptor<BookingCreatedEvent> eventCaptor = ArgumentCaptor.forClass(BookingCreatedEvent.class);
        verify(kafkaEventPublisher).publishBookingCreated(eventCaptor.capture());
        assertThat(eventCaptor.getValue().bookingId()).isEqualTo(100L);
    }

    @Test
    void createRejectsClubArmyConflict() {
        ClubTable table1 = new ClubTable("T1", true, null);
        ReflectionTestUtils.setField(table1, "id", 1L);
        User user = new User("Alice");
        ReflectionTestUtils.setField(user, "id", 10L);
        Army army = new Army(user, "Game", "Faction", true);
        ReflectionTestUtils.setField(army, "id", 9L);

        Booking overlapping = new Booking(table1, user, OffsetDateTime.now(), OffsetDateTime.now().plusHours(2));
        overlapping.setArmy(army);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(armyRepository.findById(9L)).thenReturn(Optional.of(army));
        when(bookingRepository.findOverlappingWithDetails(eq(BookingStatus.CREATED), any(), any()))
                .thenReturn(List.of(overlapping));
        when(tableRepository.findAll()).thenReturn(List.of(table1));

        BookingCreateRequest request = new BookingCreateRequest(
                null,
                user.getId(),
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(2),
                "Game",
                2,
                null,
                army.getId(),
                null
        );

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createRejectsPreferredTableNotActive() {
        ClubTable table1 = new ClubTable("T1", true, null);
        ReflectionTestUtils.setField(table1, "id", 1L);
        User user = new User("Alice");
        ReflectionTestUtils.setField(user, "id", 10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(tableRepository.findAll()).thenReturn(List.of(table1));
        when(bookingRepository.findOverlappingWithDetails(eq(BookingStatus.CREATED), any(), any()))
                .thenReturn(List.of());

        BookingCreateRequest request = new BookingCreateRequest(
                99L,
                user.getId(),
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(2),
                "Game",
                2,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createRejectsWhenNoCapacity() throws Exception {
        ClubTable table1 = new ClubTable("T1", true, null);
        ReflectionTestUtils.setField(table1, "id", 1L);
        User user = new User("Alice");
        ReflectionTestUtils.setField(user, "id", 10L);

        Booking overlapping = new Booking(table1, user, OffsetDateTime.now(), OffsetDateTime.now().plusHours(2));
        String assignments = objectMapper.writeValueAsString(List.of(Map.of("tableId", 1, "units", 2)));
        overlapping.setTableAssignments(assignments);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(tableRepository.findAll()).thenReturn(List.of(table1));
        when(bookingRepository.findOverlappingWithDetails(eq(BookingStatus.CREATED), any(), any()))
                .thenReturn(List.of(overlapping));

        BookingCreateRequest request = new BookingCreateRequest(
                null,
                user.getId(),
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(2),
                "Game",
                2,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void findOverlappingFiltersByTable() throws Exception {
        ClubTable table1 = new ClubTable("T1", true, null);
        ReflectionTestUtils.setField(table1, "id", 1L);
        ClubTable table2 = new ClubTable("T2", true, null);
        ReflectionTestUtils.setField(table2, "id", 2L);
        User user = new User("Alice");

        Booking booking1 = new Booking(table1, user, OffsetDateTime.now(), OffsetDateTime.now().plusHours(2));
        booking1.setTableAssignments(objectMapper.writeValueAsString(List.of(Map.of("tableId", 1, "units", 2))));
        Booking booking2 = new Booking(table2, user, OffsetDateTime.now(), OffsetDateTime.now().plusHours(2));
        booking2.setTableAssignments(objectMapper.writeValueAsString(List.of(Map.of("tableId", 2, "units", 1))));

        when(bookingRepository.findOverlappingWithDetails(eq(BookingStatus.CREATED), any(), any()))
                .thenReturn(List.of(booking1, booking2));

        List<Booking> result = bookingService.findOverlapping(
                OffsetDateTime.now().plusHours(1),
                OffsetDateTime.now().plusHours(3),
                1L
        );

        assertThat(result).containsExactly(booking1);
    }

    @Test
    void cancelUpdatesStatusAndPublishesEvent() {
        ClubTable table = new ClubTable("T1", true, null);
        User user = new User("Alice");
        Booking booking = new Booking(table, user, OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(2));
        ReflectionTestUtils.setField(booking, "id", 44L);

        when(bookingRepository.findById(44L)).thenReturn(Optional.of(booking));

        Booking cancelled = bookingService.cancel(44L);

        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(autoRefreshService).refreshTwoweeksIfWithinRange(booking.getStartAt());
        ArgumentCaptor<BookingCancelledEvent> eventCaptor = ArgumentCaptor.forClass(BookingCancelledEvent.class);
        verify(kafkaEventPublisher).publishBookingCancelled(eventCaptor.capture());
        assertThat(eventCaptor.getValue().bookingId()).isEqualTo(44L);
    }
}
