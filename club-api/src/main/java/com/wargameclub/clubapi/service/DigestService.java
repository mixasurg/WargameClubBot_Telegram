package com.wargameclub.clubapi.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wargameclub.clubapi.config.AppProperties;
import com.wargameclub.clubapi.dto.DigestBookingDto;
import com.wargameclub.clubapi.dto.DigestDayDto;
import com.wargameclub.clubapi.dto.DigestEventDto;
import com.wargameclub.clubapi.dto.DigestTableBookingsDto;
import com.wargameclub.clubapi.dto.WeekDigestDto;
import com.wargameclub.clubapi.entity.Booking;
import com.wargameclub.clubapi.entity.ClubEvent;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.enums.BookingStatus;
import com.wargameclub.clubapi.repository.BookingRepository;
import com.wargameclub.clubapi.repository.ClubEventRepository;
import com.wargameclub.clubapi.repository.ClubTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для работы с дайджестами.
 */
@Service
public class DigestService {
    /**
     * Поле состояния.
     */
    private static final Pattern OPPONENT_PATTERN = Pattern.compile("(?i)соперник\\s*:\\s*([^;\\n]+)");
    private static final Pattern OPPONENT_FACTION_PATTERN =
            Pattern.compile("(?i)соперник\\s+фракция\\s*:\\s*([^;\\n]+)");
    private static final Pattern OPPONENT_FACTION_ALT_PATTERN =
            Pattern.compile("(?i)фракция\\s+соперника\\s*:\\s*([^;\\n]+)");
    private static final Pattern OPPONENT_WITH_FACTION_PATTERN =
            Pattern.compile("(?i)соперник\\s*:\\s*[^;\\n()]*\\(([^)\\n]+)\\)");

    /**
     * Репозиторий бронирования.
     */
    private final BookingRepository bookingRepository;

    /**
     * Репозиторий мероприятия клуба.
     */
    private final ClubEventRepository eventRepository;

    /**
     * Репозиторий стола клуба.
     */
    private final ClubTableRepository tableRepository;

    /**
     * Параметры конфигурации App.
     */
    private final AppProperties appProperties;

    /**
     * Сериализатор JSON.
     */
    private final ObjectMapper objectMapper;

    /**
     * Выполняет операцию.
     */
    public DigestService(
            BookingRepository bookingRepository,
            ClubEventRepository eventRepository,
            ClubTableRepository tableRepository,
            AppProperties appProperties,
            ObjectMapper objectMapper
    ) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.tableRepository = tableRepository;
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * Возвращает WeekDigest.
     */
    @Transactional(readOnly = true)
    public WeekDigestDto getWeekDigest(int offset) {
        ZoneId zoneId = appProperties.getTimezone();
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime weekStart = now
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .truncatedTo(ChronoUnit.DAYS)
                .plusWeeks(offset);
        ZonedDateTime weekEnd = weekStart.plusDays(7);

        OffsetDateTime from = weekStart.toOffsetDateTime();
        OffsetDateTime to = weekEnd.toOffsetDateTime();

        List<Booking> bookings = bookingRepository.findOverlappingWithDetails(BookingStatus.CREATED, from, to);
        List<ClubEvent> events = eventRepository.findOverlappingWithOrganizer(from, to, null);
        Map<Long, String> tableNames = tableRepository.findAll().stream()
                .collect(Collectors.toMap(ClubTable::getId, ClubTable::getName));

        Map<LocalDate, Map<Long, List<Booking>>> byDay = new HashMap<>();
        for (Booking booking : bookings) {
            LocalDate date = booking.getStartAt().atZoneSameInstant(zoneId).toLocalDate();
            for (TableAllocation allocation : parseAllocations(booking)) {
                byDay.computeIfAbsent(date, key -> new HashMap<>())
                        .computeIfAbsent(allocation.tableId(), key -> new ArrayList<>())
                        .add(booking);
            }
        }

        List<DigestDayDto> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.toLocalDate().plusDays(i);
            Map<Long, List<Booking>> tableMap = byDay.getOrDefault(date, Map.of());
            List<DigestTableBookingsDto> tableDtos = tableMap.entrySet().stream()
                    .map(entry -> toTableBookings(entry.getKey(), entry.getValue(), tableNames))
                    .sorted(Comparator.comparing(DigestTableBookingsDto::tableName))
                    .collect(Collectors.toList());
            days.add(new DigestDayDto(date, tableDtos));
        }

        List<DigestEventDto> eventDtos = events.stream()
                .sorted(Comparator.comparing(ClubEvent::getStartAt))
                .map(this::toEventDto)
                .collect(Collectors.toList());

        return new WeekDigestDto(from, to, zoneId.getId(), days, eventDtos);
    }

    /**
     * Преобразует в TableBookings.
     */
    private DigestTableBookingsDto toTableBookings(Long tableId, List<Booking> bookings, Map<Long, String> tableNames) {
        bookings.sort(Comparator.comparing(Booking::getStartAt));
        List<DigestBookingDto> bookingDtos = bookings.stream()
                .map(booking -> new DigestBookingDto(
                        booking.getStartAt(),
                        booking.getEndAt(),
                        booking.getUser().getName(),
                        resolveOpponentName(booking),
                        resolveUserFaction(booking),
                        resolveOpponentFaction(booking),
                        booking.getGame(),
                        booking.getTableUnits()
                ))
                .collect(Collectors.toList());
        String tableName = tableNames.getOrDefault(tableId, "Стол-" + tableId);
        return new DigestTableBookingsDto(tableId, tableName, bookingDtos);
    }

    /**
     * Преобразует в EventDto.
     */
    private DigestEventDto toEventDto(ClubEvent event) {
        return new DigestEventDto(
                event.getId(),
                event.getTitle(),
                event.getType(),
                event.getStartAt(),
                event.getEndAt(),
                event.getOrganizer().getName(),
                event.getStatus()
        );
    }

    /**
     * Разбирает Allocations.
     */
    private List<TableAllocation> parseAllocations(Booking booking) {
        if (booking.getTableAssignments() == null || booking.getTableAssignments().isBlank()) {
            if (booking.getTable() == null) {
                return List.of();
            }
            int units = booking.getTableUnits() >= 2 ? 2 : 1;
            return List.of(new TableAllocation(booking.getTable().getId(), units));
        }
        try {
            return objectMapper.readValue(booking.getTableAssignments(), new TypeReference<List<TableAllocation>>() {
            });
        } catch (Exception ex) {
            if (booking.getTable() == null) {
                return List.of();
            }
            int units = booking.getTableUnits() >= 2 ? 2 : 1;
            return List.of(new TableAllocation(booking.getTable().getId(), units));
        }
    }

    /**
     * Определяет OpponentName.
     */
    private String resolveOpponentName(Booking booking) {
        if (booking.getOpponent() != null) {
            return booking.getOpponent().getName();
        }
        String notes = booking.getNotes();
        if (notes == null || notes.isBlank()) {
            return null;
        }
        Matcher matcher = OPPONENT_PATTERN.matcher(notes);
        if (!matcher.find()) {
            return null;
        }
        String opponent = matcher.group(1);
        return opponent != null && !opponent.isBlank() ? opponent.trim() : null;
    }

    /**
     * Определяет UserFaction.
     */
    private String resolveUserFaction(Booking booking) {
        if (booking.getArmy() == null || booking.getArmy().getFaction() == null) {
            return null;
        }
        String faction = booking.getArmy().getFaction();
        return faction != null && !faction.isBlank() ? faction.trim() : null;
    }

    /**
     * Определяет фракцию соперника.
     */
    private String resolveOpponentFaction(Booking booking) {
        String notes = booking.getNotes();
        if (notes == null || notes.isBlank()) {
            return null;
        }
        Matcher matcher = OPPONENT_FACTION_PATTERN.matcher(notes);
        if (!matcher.find()) {
            matcher = OPPONENT_FACTION_ALT_PATTERN.matcher(notes);
        }
        if (!matcher.find()) {
            matcher = OPPONENT_WITH_FACTION_PATTERN.matcher(notes);
        }
        if (!matcher.find()) {
            return null;
        }
        String faction = matcher.group(1);
        return faction != null && !faction.isBlank() ? faction.trim() : null;
    }

    /**
     * Сервис для работы с сущностью TableAllocation.
     */
    private record TableAllocation(Long tableId, int units) {
    }
}

