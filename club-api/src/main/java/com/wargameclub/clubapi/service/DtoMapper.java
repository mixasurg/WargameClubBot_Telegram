package com.wargameclub.clubapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wargameclub.clubapi.dto.ArmyDto;
import com.wargameclub.clubapi.dto.BookingDto;
import com.wargameclub.clubapi.dto.BookingTableAllocationDto;
import com.wargameclub.clubapi.dto.EventDto;
import com.wargameclub.clubapi.dto.TableDto;
import com.wargameclub.clubapi.dto.UserDto;
import com.wargameclub.clubapi.dto.UserGameStatsDto;
import com.wargameclub.clubapi.entity.Army;
import com.wargameclub.clubapi.entity.Booking;
import com.wargameclub.clubapi.entity.ClubEvent;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.entity.UserGameStats;
import com.wargameclub.clubapi.entity.User;

/**
 * Утилита для преобразования сущностей в DTO.
 */
public final class DtoMapper {
    /**
     * Сериализатор JSON для разбора назначений столов.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Закрытый конструктор утилитного класса.
     */
    private DtoMapper() {
    }

    /**
     * Преобразует пользователя в DTO.
     *
     * @param user пользователь
     * @return DTO пользователя
     */
    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getTelegramId(), user.getCreatedAt());
    }

    /**
     * Преобразует статистику игр пользователя в DTO.
     *
     * @param stats статистика пользователя
     * @return DTO статистики
     */
    public static UserGameStatsDto toUserGameStatsDto(UserGameStats stats) {
        return new UserGameStatsDto(
                stats.getUserId(),
                stats.getWins(),
                stats.getLosses(),
                stats.getDraws(),
                stats.getUpdatedAt()
        );
    }

    /**
     * Преобразует стол в DTO.
     *
     * @param table стол
     * @return DTO стола
     */
    public static TableDto toTableDto(ClubTable table) {
        return new TableDto(table.getId(), table.getName(), table.isActive(), table.getNotes());
    }

    /**
     * Преобразует бронирование в DTO.
     *
     * @param booking бронирование
     * @return DTO бронирования
     */
    public static BookingDto toBookingDto(Booking booking) {
        var assignments = parseAssignments(booking.getTableAssignments());
        return new BookingDto(
                booking.getId(),
                booking.getTable() != null ? booking.getTable().getId() : null,
                booking.getTable() != null ? booking.getTable().getName() : null,
                assignments,
                booking.getUser().getId(),
                booking.getUser().getName(),
                booking.getStartAt(),
                booking.getEndAt(),
                booking.getGame(),
                booking.getTableUnits(),
                booking.getOpponent() != null ? booking.getOpponent().getId() : null,
                booking.getOpponent() != null ? booking.getOpponent().getName() : null,
                booking.getArmy() != null ? booking.getArmy().getId() : null,
                booking.getArmy() != null ? booking.getArmy().getGame() + " / " + booking.getArmy().getFaction() : null,
                booking.getNotes(),
                booking.getBookingMode(),
                booking.getJoinDeadlineAt(),
                booking.getStatus(),
                booking.getCancelReason(),
                booking.getCreatedAt()
        );
    }

    /**
     * Преобразует мероприятие в DTO.
     *
     * @param event мероприятие
     * @return DTO мероприятия
     */
    public static EventDto toEventDto(ClubEvent event) {
        return new EventDto(
                event.getId(),
                event.getTitle(),
                event.getType(),
                event.getDescription(),
                event.getStartAt(),
                event.getEndAt(),
                event.getOrganizer().getId(),
                event.getOrganizer().getName(),
                event.getCapacity(),
                event.getStatus(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }

    /**
     * Преобразует армию в DTO.
     *
     * @param army армия
     * @return DTO армии
     */
    public static ArmyDto toArmyDto(Army army) {
        return new ArmyDto(
                army.getId(),
                army.getOwner().getId(),
                army.getOwner().getName(),
                army.getGame(),
                army.getFaction(),
                army.isClubShared(),
                army.isActive(),
                army.getCreatedAt()
        );
    }

    /**
     * Разбирает JSON-назначения столов в список DTO.
     *
     * @param json JSON-строка назначений
     * @return список назначений столов
     */
    private static java.util.List<BookingTableAllocationDto> parseAssignments(String json) {
        if (json == null || json.isBlank()) {
            return java.util.List.of();
        }
        try {
            java.util.List<TableAssignment> assignments = OBJECT_MAPPER.readValue(
                    json, new TypeReference<java.util.List<TableAssignment>>() {
                    });
            return assignments.stream()
                    .map(item -> new BookingTableAllocationDto(item.tableId(), item.units()))
                    .toList();
        } catch (Exception ex) {
            return java.util.List.of();
        }
    }

    /**
     * Внутренняя модель назначения столов для разбора JSON.
     *
     * @param tableId идентификатор стола
     * @param units количество единиц стола
     */
    private record TableAssignment(Long tableId, int units) {
    }
}
