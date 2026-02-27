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
 * Сервис формирования недельных дайджестов расписания и мероприятий.
 */
@Service
public class DigestService {
    /**
     * Шаблон поиска имени соперника в заметках.
     */
    private static final Pattern OPPONENT_PATTERN = Pattern.compile("(?i)соперник\\s*:\\s*([^;\\n]+)");
    private static final Pattern OPPONENT_FACTION_PATTERN =
            Pattern.compile("(?i)соперник\\s+фракция\\s*:\\s*([^;\\n]+)");
    private static final Pattern OPPONENT_FACTION_ALT_PATTERN =
            Pattern.compile("(?i)фракция\\s+соперника\\s*:\\s*([^;\\n]+)");
    private static final Pattern OPPONENT_WITH_FACTION_PATTERN =
            Pattern.compile("(?i)соперник\\s*:\\s*[^;\\n()]*\\(([^)\\n]+)\\)");

    /**
     * Репозиторий бронирований.
     */
    private final BookingRepository bookingRepository;

    /**
     * Репозиторий мероприятий клуба.
     */
    private final ClubEventRepository eventRepository;

    /**
     * Репозиторий столов клуба.
     */
    private final ClubTableRepository tableRepository;

    /**
     * Настройки приложения.
     */
    private final AppProperties appProperties;

    /**
     * Сериализатор JSON.
     */
    private final ObjectMapper objectMapper;

    /**
     * Создает сервис дайджестов.
     *
     * @param bookingRepository репозиторий бронирований
     * @param eventRepository репозиторий мероприятий
     * @param tableRepository репозиторий столов
     * @param appProperties настройки приложения
     * @param objectMapper сериализатор JSON
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
     * Формирует недельный дайджест относительно текущей недели.
     *
     * @param offset смещение недели (0 — текущая, 1 — следующая)
     * @return DTO недельного дайджеста
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
     * Преобразует список бронирований по столу в DTO.
     *
     * @param tableId идентификатор стола
     * @param bookings список бронирований
     * @param tableNames карта идентификатор–название стола
     * @return DTO бронирований по столу
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
     * Преобразует мероприятие в DTO для дайджеста.
     *
     * @param event мероприятие
     * @return DTO мероприятия
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
     * Разбирает назначения столов из JSON.
     *
     * @param booking бронирование
     * @return список назначений столов
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
     * Определяет имя соперника из бронирования или заметок.
     *
     * @param booking бронирование
     * @return имя соперника или null
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
     * Определяет фракцию основного игрока.
     *
     * @param booking бронирование
     * @return фракция или null
     */
    private String resolveUserFaction(Booking booking) {
        if (booking.getArmy() == null || booking.getArmy().getFaction() == null) {
            return null;
        }
        String faction = booking.getArmy().getFaction();
        return faction != null && !faction.isBlank() ? faction.trim() : null;
    }

    /**
     * Определяет фракцию соперника из заметок.
     *
     * @param booking бронирование
     * @return фракция соперника или null
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
     * Назначение части бронирования на конкретный стол.
     *
     * @param tableId идентификатор стола
     * @param units количество единиц стола
     */
    private record TableAllocation(Long tableId, int units) {
    }
}
