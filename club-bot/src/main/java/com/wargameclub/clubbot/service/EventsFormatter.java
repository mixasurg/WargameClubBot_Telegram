package com.wargameclub.clubbot.service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.wargameclub.clubbot.dto.EventDto;
import org.springframework.stereotype.Component;

/**
 * Форматирует список мероприятий для отправки в Telegram.
 */
@Component
public class EventsFormatter {
    /**
     * Формат даты и времени.
     */
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Формирует текст списка ближайших мероприятий.
     *
     * @param events список мероприятий
     * @param timezone часовой пояс
     * @return форматированный текст
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
