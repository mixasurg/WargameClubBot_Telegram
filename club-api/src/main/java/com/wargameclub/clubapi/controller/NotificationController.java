package com.wargameclub.clubapi.controller;

import java.util.List;
import java.util.UUID;
import com.wargameclub.clubapi.dto.NotificationFailRequest;
import com.wargameclub.clubapi.dto.NotificationOutboxDto;
import com.wargameclub.clubapi.enums.NotificationTarget;
import com.wargameclub.clubapi.service.NotificationOutboxService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для управления уведомлениями.
 */
@RestController
@Validated
@RequestMapping("/api/notifications")
public class NotificationController {

    /**
     * Сервис NotificationOutbox.
     */
    private final NotificationOutboxService notificationService;

    /**
     * Создает контроллер для работы с очередью уведомлений.
     *
     * @param notificationService сервис исходящих уведомлений
     */
    public NotificationController(NotificationOutboxService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Возвращает список ожидающих отправки уведомлений.
     *
     * @param target целевой канал уведомления
     * @param limit максимальное число возвращаемых уведомлений
     * @return список уведомлений к отправке
     */
    @GetMapping("/pending")
    public List<NotificationOutboxDto> pending(
            @RequestParam(name = "target") NotificationTarget target,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        return notificationService.getPending(target, limit);
    }

    /**
     * Отмечает уведомление как отправленное.
     *
     * @param id идентификатор уведомления
     */
    @PostMapping("/{id}/ack")
    public void ack(@PathVariable("id") UUID id) {
        notificationService.markSent(id);
    }

    /**
     * Отмечает уведомление как ошибочное и сохраняет причину.
     *
     * @param id идентификатор уведомления
     * @param request данные об ошибке
     */
    @PostMapping("/{id}/fail")
    public void fail(@PathVariable("id") UUID id, @Valid @RequestBody NotificationFailRequest request) {
        notificationService.markFailed(id, request.error());
    }
}
