package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wargameclub.clubapi.entity.Booking;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.enums.BookingStatus;
import com.wargameclub.clubapi.exception.BadRequestException;
import com.wargameclub.clubapi.exception.ConflictException;
import com.wargameclub.clubapi.exception.NotFoundException;
import com.wargameclub.clubapi.repository.BookingRepository;
import com.wargameclub.clubapi.repository.ClubTableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис распределения бронирований по столам.
 */
@Service
public class TableAllocationService {

    /**
     * Логгер.
     */
    private static final Logger log = LoggerFactory.getLogger(TableAllocationService.class);

    /**
     * Емкость одного стола в условных единицах.
     */
    private static final int TABLE_CAPACITY_UNITS = 2;

    /**
     * Репозиторий бронирований.
     */
    private final BookingRepository bookingRepository;

    /**
     * Репозиторий столов клуба.
     */
    private final ClubTableRepository tableRepository;

    /**
     * Сериализатор JSON.
     */
    private final ObjectMapper objectMapper;

    /**
     * Создает сервис распределения столов.
     *
     * @param bookingRepository репозиторий бронирований
     * @param tableRepository репозиторий столов
     * @param objectMapper сериализатор JSON
     */
    public TableAllocationService(
            BookingRepository bookingRepository,
            ClubTableRepository tableRepository,
            ObjectMapper objectMapper
    ) {
        this.bookingRepository = bookingRepository;
        this.tableRepository = tableRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Рассчитывает распределение столов для нового бронирования.
     * Метод должен вызываться внутри уже открытой транзакции.
     *
     * @param from начало интервала
     * @param to конец интервала
     * @param requestedUnits требуемые единицы столов
     * @param preferredTableId предпочитаемый стол (опционально)
     * @return снимок занятости и рассчитанных назначений
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public AllocationSnapshot allocateForRange(
            OffsetDateTime from,
            OffsetDateTime to,
            int requestedUnits,
            Long preferredTableId
    ) {
        List<ClubTable> activeTables = tableRepository.findActiveForUpdate().stream()
                .sorted(Comparator.comparing(ClubTable::getId))
                .toList();
        if (activeTables.isEmpty()) {
            throw new NotFoundException("Столы не настроены");
        }
        if (preferredTableId != null && activeTables.stream().noneMatch(table -> table.getId().equals(preferredTableId))) {
            throw new BadRequestException("Предпочтительный стол неактивен или отсутствует");
        }

        List<Booking> overlapping = bookingRepository.findOverlappingWithDetails(BookingStatus.CREATED, from, to);
        List<TableAllocation> allocations = allocateTables(overlapping, activeTables, requestedUnits, preferredTableId);
        return new AllocationSnapshot(overlapping, allocations);
    }

    /**
     * Проверяет, содержит ли бронирование указанный стол.
     *
     * @param booking бронирование
     * @param tableId идентификатор стола
     * @return true, если стол присутствует в назначениях
     */
    public boolean bookingHasTable(Booking booking, Long tableId) {
        return parseAllocations(booking).stream()
                .anyMatch(allocation -> allocation.tableId().equals(tableId));
    }

    /**
     * Разбирает сохраненные назначения столов из JSON.
     *
     * @param booking бронирование
     * @return список назначений столов
     */
    public List<TableAllocation> parseAllocations(Booking booking) {
        if (booking == null) {
            return List.of();
        }
        if (booking.getTableAssignments() == null || booking.getTableAssignments().isBlank()) {
            return fallbackAllocations(booking);
        }
        try {
            return objectMapper.readValue(
                    booking.getTableAssignments(),
                    new TypeReference<List<TableAllocation>>() {
                    }
            );
        } catch (JsonProcessingException ex) {
            log.warn("Не удалось разобрать tableAssignments для bookingId={}. Будет использован fallback.",
                    booking.getId(), ex);
            return fallbackAllocations(booking);
        }
    }

    /**
     * Сериализует назначения столов в JSON.
     *
     * @param allocations список назначений
     * @return JSON-строка
     */
    public String serializeAllocations(List<TableAllocation> allocations) {
        try {
            return objectMapper.writeValueAsString(allocations);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Не удалось сериализовать назначения столов");
        }
    }

    /**
     * Распределяет требуемые единицы столов с учетом текущих бронирований.
     *
     * @param overlapping пересекающиеся бронирования
     * @param tables активные столы
     * @param requestedUnits требуемые единицы столов
     * @param preferredTableId предпочитаемый стол (опционально)
     * @return список распределений по столам
     */
    private List<TableAllocation> allocateTables(
            List<Booking> overlapping,
            List<ClubTable> tables,
            int requestedUnits,
            Long preferredTableId
    ) {
        Map<Long, Integer> usedUnits = new LinkedHashMap<>();
        for (ClubTable table : tables) {
            usedUnits.put(table.getId(), 0);
        }

        for (Booking booking : overlapping) {
            for (TableAllocation allocation : parseAllocations(booking)) {
                if (usedUnits.containsKey(allocation.tableId())) {
                    usedUnits.merge(allocation.tableId(), allocation.units(), Integer::sum);
                }
            }
        }

        List<Integer> requiredAllocations = new ArrayList<>(expandAllocations(requestedUnits));
        List<TableAllocation> allocations = new ArrayList<>();

        if (preferredTableId != null && !requiredAllocations.isEmpty()) {
            int preferredUnits = requiredAllocations.contains(TABLE_CAPACITY_UNITS) ? TABLE_CAPACITY_UNITS : 1;
            if (!canAllocate(preferredTableId, usedUnits, preferredUnits)) {
                throw new ConflictException("Предпочтительный стол недоступен");
            }
            allocations.add(new TableAllocation(preferredTableId, preferredUnits));
            usedUnits.merge(preferredTableId, preferredUnits, Integer::sum);
            requiredAllocations.remove(Integer.valueOf(preferredUnits));
        }

        for (int units : requiredAllocations) {
            Long tableId = findBestTable(usedUnits, units);
            if (tableId == null) {
                throw new ConflictException("Недостаточно свободных столов на это время");
            }
            allocations.add(new TableAllocation(tableId, units));
            usedUnits.merge(tableId, units, Integer::sum);
        }
        return allocations;
    }

    /**
     * Проверяет, можно ли разместить указанное количество единиц на столе.
     *
     * @param tableId идентификатор стола
     * @param usedUnits текущая занятость по столам
     * @param units требуемые единицы
     * @return true, если размещение возможно
     */
    private boolean canAllocate(Long tableId, Map<Long, Integer> usedUnits, int units) {
        int used = usedUnits.getOrDefault(tableId, 0);
        return used + units <= TABLE_CAPACITY_UNITS;
    }

    /**
     * Подбирает наиболее подходящий стол с учетом требуемых единиц.
     *
     * @param usedUnits текущая занятость по столам
     * @param units требуемые единицы
     * @return идентификатор стола или null, если нет доступных
     */
    private Long findBestTable(Map<Long, Integer> usedUnits, int units) {
        if (units == TABLE_CAPACITY_UNITS) {
            return usedUnits.entrySet().stream()
                    .filter(entry -> entry.getValue() + units <= TABLE_CAPACITY_UNITS)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
        }
        return usedUnits.entrySet().stream()
                .filter(entry -> entry.getValue() + units <= TABLE_CAPACITY_UNITS)
                .sorted(Comparator.comparingInt(entry -> entry.getValue() == 1 ? 0 : 1))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Разворачивает запрошенное количество единиц в список распределений по столам.
     *
     * @param requestedUnits запрошенные единицы столов
     * @return список единиц для распределения
     */
    private List<Integer> expandAllocations(int requestedUnits) {
        List<Integer> allocations = new ArrayList<>();
        if (requestedUnits == 1) {
            allocations.add(1);
        } else if (requestedUnits == 2) {
            allocations.add(2);
        } else if (requestedUnits == 3) {
            allocations.add(2);
            allocations.add(2);
        } else if (requestedUnits % 2 == 0) {
            int tables = requestedUnits / 2;
            for (int i = 0; i < tables; i++) {
                allocations.add(2);
            }
        } else {
            throw new BadRequestException("Неподдерживаемое значение tableUnits");
        }
        return allocations;
    }

    /**
     * Возвращает fallback-назначение по полям legacy-брони.
     *
     * @param booking бронирование
     * @return fallback-назначение
     */
    private List<TableAllocation> fallbackAllocations(Booking booking) {
        if (booking.getTable() == null) {
            return List.of();
        }
        int units = booking.getTableUnits() >= 2 ? 2 : 1;
        return List.of(new TableAllocation(booking.getTable().getId(), units));
    }

    /**
     * Снимок распределения для бронирования: текущие пересечения и расчет назначений.
     *
     * @param overlappingBookings пересекающиеся бронирования
     * @param allocations рассчитанные назначения столов
     */
    public record AllocationSnapshot(
            List<Booking> overlappingBookings,
            List<TableAllocation> allocations
    ) {
    }

    /**
     * Назначение части бронирования на конкретный стол.
     *
     * @param tableId идентификатор стола
     * @param units количество единиц стола
     */
    public record TableAllocation(Long tableId, int units) {
    }
}
