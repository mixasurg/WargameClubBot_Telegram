package com.wargameclub.clubapi.service;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wargameclub.clubapi.config.AppProperties;
import com.wargameclub.clubapi.dto.DigestBookingDto;
import com.wargameclub.clubapi.dto.WeekDigestDto;
import com.wargameclub.clubapi.entity.Booking;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.enums.BookingStatus;
import com.wargameclub.clubapi.repository.BookingRepository;
import com.wargameclub.clubapi.repository.ClubEventRepository;
import com.wargameclub.clubapi.repository.ClubTableRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Модульные проверки парсинга соперника в дайджесте.
 */
class DigestServiceTest {
    /**
     * Часовой пояс, используемый в тестах.
     */
    private static final ZoneId TEST_ZONE = ZoneId.of("Europe/Moscow");

    /**
     * Проверяет парсинг соперника и фракции из англоязычных заметок.
     */
    @Test
    void getWeekDigest_parsesOpponentFromEnglishNotes() {
        ClubTable table = new ClubTable("Стол 1", true, null);
        ReflectionTestUtils.setField(table, "id", 1L);
        User user = new User("mixasurg");
        OffsetDateTime start = currentWeekStart().plusHours(8).plusMinutes(30).toOffsetDateTime();
        OffsetDateTime end = start.plusHours(3);

        Booking booking = new Booking(table, user, start, end);
        booking.setGame("Warhammer 40K");
        booking.setTableUnits(2);
        booking.setNotes("Opponent: ElderDragonLoaf; Opponent faction: Tyranids;");

        DigestService service = createService(List.of(booking), List.of(table));
        WeekDigestDto digest = service.getWeekDigest(0);
        DigestBookingDto bookingDto = firstBooking(digest);

        assertThat(bookingDto.opponentName()).isEqualTo("ElderDragonLoaf");
        assertThat(bookingDto.opponentFaction()).isEqualTo("Tyranids");
    }

    /**
     * Проверяет извлечение фракции соперника из имени в скобках.
     */
    @Test
    void getWeekDigest_extractsFactionFromBracketedOpponent() {
        ClubTable table = new ClubTable("Стол 1", true, null);
        ReflectionTestUtils.setField(table, "id", 1L);
        User user = new User("mixasurg");
        OffsetDateTime start = currentWeekStart().plusHours(8).plusMinutes(30).toOffsetDateTime();
        OffsetDateTime end = start.plusHours(3);

        Booking booking = new Booking(table, user, start, end);
        booking.setGame("Warhammer 40K");
        booking.setTableUnits(2);
        booking.setNotes("Соперник: ElderDragonLoaf (Tyranids);");

        DigestService service = createService(List.of(booking), List.of(table));
        WeekDigestDto digest = service.getWeekDigest(0);
        DigestBookingDto bookingDto = firstBooking(digest);

        assertThat(bookingDto.opponentName()).isEqualTo("ElderDragonLoaf");
        assertThat(bookingDto.opponentFaction()).isEqualTo("Tyranids");
    }

    /**
     * Проверяет, что связанный соперник имеет приоритет над заметками.
     */
    @Test
    void getWeekDigest_prefersLinkedOpponentName() {
        ClubTable table = new ClubTable("Стол 1", true, null);
        ReflectionTestUtils.setField(table, "id", 1L);
        User user = new User("mixasurg");
        User opponent = new User("ElderDragonLoaf");
        OffsetDateTime start = currentWeekStart().plusHours(8).plusMinutes(30).toOffsetDateTime();
        OffsetDateTime end = start.plusHours(3);

        Booking booking = new Booking(table, user, start, end);
        booking.setGame("Warhammer 40K");
        booking.setTableUnits(2);
        booking.setOpponent(opponent);
        booking.setNotes("Соперник фракция: Tyranids;");

        DigestService service = createService(List.of(booking), List.of(table));
        WeekDigestDto digest = service.getWeekDigest(0);
        DigestBookingDto bookingDto = firstBooking(digest);

        assertThat(bookingDto.opponentName()).isEqualTo("ElderDragonLoaf");
        assertThat(bookingDto.opponentFaction()).isEqualTo("Tyranids");
    }

    /**
     * Создает сервис дайджестов с мок-репозиториями.
     *
     * @param bookings список бронирований
     * @param tables список столов
     * @return сервис дайджестов
     */
    private DigestService createService(List<Booking> bookings, List<ClubTable> tables) {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        ClubEventRepository eventRepository = mock(ClubEventRepository.class);
        ClubTableRepository tableRepository = mock(ClubTableRepository.class);
        AppProperties appProperties = new AppProperties();
        appProperties.setTimezone(TEST_ZONE);
        when(bookingRepository.findOverlappingWithDetails(eq(BookingStatus.CREATED), any(OffsetDateTime.class), any(
                OffsetDateTime.class))).thenReturn(bookings);
        when(eventRepository.findOverlappingWithOrganizer(any(OffsetDateTime.class), any(OffsetDateTime.class), isNull()))
                .thenReturn(List.of());
        when(tableRepository.findAll()).thenReturn(tables);
        TableAllocationService tableAllocationService = new TableAllocationService(
                bookingRepository,
                tableRepository,
                new ObjectMapper()
        );
        return new DigestService(bookingRepository, eventRepository, tableRepository, appProperties, tableAllocationService);
    }

    /**
     * Возвращает первое бронирование из дайджеста.
     *
     * @param digest дайджест недели
     * @return первое бронирование
     */
    private DigestBookingDto firstBooking(WeekDigestDto digest) {
        return digest.days().stream()
                .flatMap(day -> day.tables().stream())
                .flatMap(table -> table.bookings().stream())
                .findFirst()
                .orElseThrow();
    }

    /**
     * Возвращает начало текущей недели в тестовом часовом поясе.
     *
     * @return начало недели
     */
    private ZonedDateTime currentWeekStart() {
        return ZonedDateTime.now(TEST_ZONE)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .truncatedTo(ChronoUnit.DAYS);
    }
}
