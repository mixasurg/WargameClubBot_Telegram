package com.wargameclub.clubbot.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubbot.dto.BookingMode;
import com.wargameclub.clubbot.dto.DigestBookingDto;
import com.wargameclub.clubbot.dto.DigestDayDto;
import com.wargameclub.clubbot.dto.DigestEventDto;
import com.wargameclub.clubbot.dto.DigestTableBookingsDto;
import com.wargameclub.clubbot.dto.WeekDigestDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Модульные проверки форматирования расписания.
 */
class DigestFormatterTest {

    /**
     * Проверяет формат строки бронирования с фракцией соперника и количеством столов.
     */
    @Test
    void format_includesOpponentFactionAndTableUnits() {
        DigestFormatter formatter = new DigestFormatter();
        OffsetDateTime weekStart = OffsetDateTime.parse("2026-02-23T00:00:00+03:00");
        OffsetDateTime weekEnd = OffsetDateTime.parse("2026-03-02T00:00:00+03:00");

        DigestBookingDto booking = new DigestBookingDto(
                15L,
                OffsetDateTime.parse("2026-02-23T10:00:00+03:00"),
                OffsetDateTime.parse("2026-02-23T19:48:00+03:00"),
                "mixasurg",
                "ElderDragonLoaf",
                "Blood Angels",
                "Tyranids",
                "Warhammer 40K",
                2,
                BookingMode.FIXED
        );
        DigestTableBookingsDto table = new DigestTableBookingsDto(1L, "Стол 1", List.of(booking));
        DigestDayDto day = new DigestDayDto(LocalDate.of(2026, 2, 23), List.of(table));

        WeekDigestDto digest = new WeekDigestDto(
                weekStart,
                weekEnd,
                "Europe/Moscow",
                List.of(day),
                List.of()
        );

        String result = formatter.format(digest);

        assertThat(result).contains(
                "10:00-19:48 @mixasurg (Blood Angels) vs @ElderDragonLoaf (Tyranids) - Warhammer 40K - столы: 1"
        );
    }

    /**
     * Проверяет текст при отсутствии бронирований и мероприятий.
     */
    @Test
    void format_whenNoBookingsOrEvents() {
        DigestFormatter formatter = new DigestFormatter();
        WeekDigestDto digest = new WeekDigestDto(
                OffsetDateTime.parse("2026-02-23T00:00:00+03:00"),
                OffsetDateTime.parse("2026-03-02T00:00:00+03:00"),
                "Europe/Moscow",
                List.of(),
                List.of()
        );

        String result = formatter.format(digest);

        assertThat(result).contains("- Бронирований нет.");
        assertThat(result).contains("- Мероприятий нет.");
    }

    /**
     * Проверяет, что в строке мероприятия выводится описание.
     */
    @Test
    void format_includesEventDescription() {
        DigestFormatter formatter = new DigestFormatter();
        DigestEventDto event = new DigestEventDto(
                7L,
                "Ы",
                "PAINT_DAY",
                "Покрас и сборка",
                OffsetDateTime.parse("2026-03-10T17:00:00+03:00"),
                OffsetDateTime.parse("2026-03-10T20:00:00+03:00"),
                "ssttaayy0_o",
                "SCHEDULED"
        );
        WeekDigestDto digest = new WeekDigestDto(
                OffsetDateTime.parse("2026-03-09T00:00:00+03:00"),
                OffsetDateTime.parse("2026-03-16T00:00:00+03:00"),
                "Europe/Moscow",
                List.of(),
                List.of(event)
        );

        String result = formatter.format(digest);

        assertThat(result).contains(
                "- 10.03.2026 17:00-10.03.2026 20:00 | Ы (День покраски) | Описание: Покрас и сборка | Организатор: ssttaayy0_o | Статус: Запланировано"
        );
    }
}
