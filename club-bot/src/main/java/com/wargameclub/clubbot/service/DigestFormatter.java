package com.wargameclub.clubbot.service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import com.wargameclub.clubbot.dto.DigestBookingDto;
import com.wargameclub.clubbot.dto.DigestDayDto;
import com.wargameclub.clubbot.dto.DigestEventDto;
import com.wargameclub.clubbot.dto.DigestTableBookingsDto;
import com.wargameclub.clubbot.dto.WeekDigestDto;
import org.springframework.stereotype.Component;

/**
 * Форматирует недельный дайджест в текст для отправки в Telegram.
 */
@Component
public class DigestFormatter {
    /**
     * Формат даты (дд.ММ.гггг).
     */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    /**
     * Формат времени (ЧЧ:мм).
     */
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    /**
     * Формат дня недели (краткий, на русском).
     */
    private static final DateTimeFormatter DAY_FORMAT =
            DateTimeFormatter.ofPattern("EEE", new Locale("ru", "RU"));

    /**
     * Формирует текст дайджеста с бронированиями и мероприятиями.
     *
     * @param digest недельный дайджест
     * @return форматированный текст
     */
    public String format(WeekDigestDto digest) {
        StringBuilder sb = new StringBuilder();
        ZoneId zoneId = resolveDigestZone(digest);
        String weekStart = digest.weekStart().atZoneSameInstant(zoneId).toLocalDate().format(DATE_FORMAT);
        String weekEnd = digest.weekEnd().atZoneSameInstant(zoneId).toLocalDate().minusDays(1).format(DATE_FORMAT);
        sb.append("Расписание ").append(weekStart).append(" - ").append(weekEnd)
                .append(" (").append(digest.timezone()).append(")\n");
        sb.append("========================================\n");
        sb.append("Бронирования\n");
        boolean hasBookings = false;
        if (digest.days() != null && !digest.days().isEmpty()) {
            for (DigestDayDto day : digest.days()) {
                if (day.tables() == null || day.tables().isEmpty()) {
                    continue;
                }
                hasBookings = true;
                sb.append("- ").append(day.date().format(DATE_FORMAT))
                        .append(" (").append(day.date().format(DAY_FORMAT)).append(")").append("\n");
                for (DigestTableBookingsDto table : day.tables()) {
                    sb.append("  - ").append(table.tableName()).append(":\n");
                    for (DigestBookingDto booking : table.bookings()) {
                        String start = booking.startAt().toLocalTime().format(TIME_FORMAT);
                        String end = booking.endAt().toLocalTime().format(TIME_FORMAT);
                        String userLabel = formatPlayer(booking.userName(), booking.userFaction());
                        sb.append("    - ").append(start).append("-").append(end)
                                .append(" ").append(userLabel);
                        if (booking.opponentName() != null && !booking.opponentName().isBlank()) {
                            String opponentLabel = formatPlayer(booking.opponentName(), booking.opponentFaction());
                            sb.append(" vs ").append(opponentLabel);
                        }
                        if (booking.game() != null && !booking.game().isBlank()) {
                            sb.append(" - ").append(booking.game());
                        }
                        if (booking.tableUnits() != null) {
                            sb.append(" - столы: ").append(formatTableUnits(booking.tableUnits()));
                        }
                        sb.append("\n");
                    }
                }
            }
        }
        if (!hasBookings) {
            sb.append("- Бронирований нет.\n");
        }

        sb.append("\nМероприятия\n");
        if (digest.events() == null || digest.events().isEmpty()) {
            sb.append("- Мероприятий нет.\n");
        } else {
            for (DigestEventDto event : digest.events()) {
                String start = event.startAt().atZoneSameInstant(ZoneId.of(digest.timezone())).format(
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                );
                String end = event.endAt().atZoneSameInstant(ZoneId.of(digest.timezone())).format(
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                );
                String typeLabel = formatEventTypeLabel(event.type());
                String statusLabel = formatEventStatus(event.status());
                sb.append("- ").append(start).append("-").append(end)
                        .append(" | ").append(event.title())
                        .append(" (").append(typeLabel).append(")")
                        .append(" | Организатор: ").append(event.organizerName())
                        .append(" | Статус: ").append(statusLabel)
                        .append("\n");
            }
        }

        return sb.toString().trim();
    }

    /**
     * Определяет часовой пояс для отображения дайджеста.
     *
     * @param digest дайджест
     * @return часовой пояс
     */
    private ZoneId resolveDigestZone(WeekDigestDto digest) {
        if (digest != null && digest.timezone() != null && !digest.timezone().isBlank()) {
            try {
                return ZoneId.of(digest.timezone());
            } catch (Exception ex) {
                return ZoneId.of("Europe/Moscow");
            }
        }
        return ZoneId.of("Europe/Moscow");
    }

    /**
     * Форматирует количество единиц стола в человекочитаемый вид.
     *
     * @param units количество единиц
     * @return форматированная строка
     */
    private String formatTableUnits(int units) {
        return switch (units) {
            case 1 -> "0.5";
            case 2 -> "1";
            case 3 -> "1.5";
            case 4 -> "2";
            case 6 -> "3";
            default -> String.valueOf(units);
        };
    }

    /**
     * Формирует отображаемое имя игрока с фракцией.
     *
     * @param name имя игрока
     * @param faction фракция игрока
     * @return форматированная строка
     */
    private String formatPlayer(String name, String faction) {
        if (name == null || name.isBlank()) {
            return "-";
        }
        String displayName = formatDisplayName(name);
        if (faction == null || faction.isBlank()) {
            return displayName;
        }
        return displayName + " (" + faction + ")";
    }

    /**
     * Форматирует имя пользователя с учетом Telegram-ника.
     *
     * @param name имя пользователя
     * @return отображаемое имя
     */
    private String formatDisplayName(String name) {
        String trimmed = name.trim();
        if (trimmed.startsWith("@")) {
            return trimmed;
        }
        if (!trimmed.contains(" ") && trimmed.matches("[A-Za-z0-9_]+")) {
            return "@" + trimmed;
        }
        return trimmed;
    }

    /**
     * Форматирует тип мероприятия в человекочитаемый вид.
     *
     * @param type тип мероприятия
     * @return строковое представление типа
     */
    private String formatEventTypeLabel(String type) {
        if (type == null) {
            return "-";
        }
        return switch (type) {
            case "PAINT_DAY" -> "День покраски";
            case "WORK_DAY" -> "Рабочий день";
            case "TOURNAMENT" -> "Турнир";
            case "OTHER" -> "Другое";
            default -> type;
        };
    }

    /**
     * Форматирует статус мероприятия в человекочитаемый вид.
     *
     * @param status статус мероприятия
     * @return строковое представление статуса
     */
    private String formatEventStatus(String status) {
        if (status == null) {
            return "-";
        }
        return switch (status) {
            case "SCHEDULED" -> "Запланировано";
            case "CANCELLED" -> "Отменено";
            default -> status;
        };
    }
}
