package com.wargameclub.clubbot.service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.wargameclub.clubbot.dto.EventDto;
import org.springframework.stereotype.Component;

/**
 * Сервис для работы с сущностью Events.
 */
@Component
public class EventsFormatter {
    /**
     * Поле состояния.
     */
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Форматирует Upcoming.
     */
    public String formatUpcoming(List<EventDto> events, String timezone) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ближайшие мероприятия (14 дней)");
        if (events == null || events.isEmpty()) {
            sb.append("\nМероприятий нет.");
            return sb.toString();
        }
        ZoneId zoneId = ZoneId.of(timezone);
        sb.append("\n");
        for (EventDto event : events) {
            String start = event.startAt().atZoneSameInstant(zoneId).format(DATE_TIME);
            String end = event.endAt().atZoneSameInstant(zoneId).format(DATE_TIME);
            String typeLabel = formatEventTypeLabel(event.type());
            String statusLabel = formatEventStatus(event.status());
            sb.append("- ").append(event.title())
                    .append(" (")
                    .append(typeLabel)
                    .append(") ")
                    .append(start)
                    .append("-")
                    .append(end)
                    .append(" Организатор: ")
                    .append(event.organizerName())
                    .append(" Статус: ")
                    .append(statusLabel)
                    .append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * Форматирует EventTypeLabel.
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
     * Форматирует EventStatus.
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

