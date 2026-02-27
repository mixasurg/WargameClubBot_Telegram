package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.dto.BookingDto;
import com.wargameclub.clubapi.entity.Army;
import com.wargameclub.clubapi.entity.Booking;
import com.wargameclub.clubapi.entity.ClubTable;
import com.wargameclub.clubapi.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Модульные тесты преобразований {@link DtoMapper}.
 */
class DtoMapperTest {

    /**
     * Проверяет разбор JSON назначений столов и маппинг соперника/армии.
     */
    @Test
    void toBookingDto_mapsAssignmentsOpponentAndArmy() {
        User user = new User("mixasurg");
        ReflectionTestUtils.setField(user, "id", 1L);
        User opponent = new User("ElderDragonLoaf");
        ReflectionTestUtils.setField(opponent, "id", 2L);

        ClubTable table = new ClubTable("Стол 1", true, null);
        ReflectionTestUtils.setField(table, "id", 10L);

        Army army = new Army(user, "Warhammer 40K", "Blood Angels", false);
        ReflectionTestUtils.setField(army, "id", 7L);

        OffsetDateTime start = OffsetDateTime.parse("2026-02-23T10:00:00+03:00");
        OffsetDateTime end = OffsetDateTime.parse("2026-02-23T19:48:00+03:00");
        Booking booking = new Booking(table, user, start, end);
        ReflectionTestUtils.setField(booking, "id", 5L);
        booking.setOpponent(opponent);
        booking.setArmy(army);
        booking.setGame("Warhammer 40K");
        booking.setTableUnits(2);
        booking.setTableAssignments("[{\"tableId\":10,\"units\":2}]");

        BookingDto dto = DtoMapper.toBookingDto(booking);

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.tableAssignments()).hasSize(1);
        assertThat(dto.tableAssignments().get(0).tableId()).isEqualTo(10L);
        assertThat(dto.tableAssignments().get(0).units()).isEqualTo(2);
        assertThat(dto.opponentUserId()).isEqualTo(2L);
        assertThat(dto.opponentName()).isEqualTo("ElderDragonLoaf");
        assertThat(dto.armyId()).isEqualTo(7L);
        assertThat(dto.armyName()).isEqualTo("Warhammer 40K / Blood Angels");
    }

    /**
     * Проверяет корректное поведение при пустых назначениях столов.
     */
    @Test
    void toBookingDto_handlesBlankAssignments() {
        User user = new User("mixasurg");
        ReflectionTestUtils.setField(user, "id", 1L);
        ClubTable table = new ClubTable("Стол 1", true, null);
        ReflectionTestUtils.setField(table, "id", 10L);

        OffsetDateTime start = OffsetDateTime.parse("2026-02-23T10:00:00+03:00");
        OffsetDateTime end = OffsetDateTime.parse("2026-02-23T19:48:00+03:00");
        Booking booking = new Booking(table, user, start, end);
        booking.setTableAssignments(" ");

        BookingDto dto = DtoMapper.toBookingDto(booking);

        assertThat(dto.tableAssignments()).isEmpty();
    }
}
