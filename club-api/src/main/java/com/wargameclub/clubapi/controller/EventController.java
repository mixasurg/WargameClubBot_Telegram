package com.wargameclub.clubapi.controller;

import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubapi.dto.EventAttendanceConfirmRequest;
import com.wargameclub.clubapi.dto.EventCreateRequest;
import com.wargameclub.clubapi.dto.EventDto;
import com.wargameclub.clubapi.dto.EventRegistrationRequest;
import com.wargameclub.clubapi.dto.EventUpdateRequest;
import com.wargameclub.clubapi.entity.ClubEvent;
import com.wargameclub.clubapi.enums.EventType;
import com.wargameclub.clubapi.service.DtoMapper;
import com.wargameclub.clubapi.service.EventService;
import com.wargameclub.clubapi.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для управления мероприятиями.
 */
@RestController
@Validated
@RequestMapping("/api/events")
public class EventController {

    /**
     * Сервис мероприятия.
     */
    private final EventService eventService;

    /**
     * Сервис пользователя.
     */
    private final UserService userService;

    /**
     * Создает контроллер для операций над мероприятиями.
     *
     * @param eventService сервис мероприятий
     * @param userService сервис пользователей
     */
    public EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    /**
     * Создает новое мероприятие клуба.
     *
     * @param request данные для создания мероприятия
     * @return созданное мероприятие
     */
    @PostMapping
    public EventDto create(@Valid @RequestBody EventCreateRequest request) {
        ClubEvent event = new ClubEvent();
        event.setTitle(request.title());
        event.setType(request.type());
        event.setDescription(request.description());
        event.setStartAt(request.startAt());
        event.setEndAt(request.endAt());
        event.setOrganizer(userService.getById(request.organizerUserId()));
        event.setCapacity(request.capacity());
        return DtoMapper.toEventDto(eventService.create(event));
    }

    /**
     * Обновляет существующее мероприятие.
     *
     * @param id идентификатор мероприятия
     * @param request данные для обновления
     * @return обновленное мероприятие
     */
    @PutMapping("/{id}")
    public EventDto update(@PathVariable("id") Long id, @Valid @RequestBody EventUpdateRequest request) {
        return DtoMapper.toEventDto(eventService.update(id, request));
    }

    /**
     * Возвращает список мероприятий, пересекающих заданный интервал.
     *
     * @param from начало интервала
     * @param to конец интервала
     * @param type фильтр по типу мероприятия (опционально)
     * @return список мероприятий
     */
    @GetMapping
    public List<EventDto> list(
            @RequestParam(name = "from") @NotNull OffsetDateTime from,
            @RequestParam(name = "to") @NotNull OffsetDateTime to,
            @RequestParam(name = "type", required = false) EventType type
    ) {
        return eventService.findOverlapping(from, to, type).stream()
                .map(DtoMapper::toEventDto)
                .toList();
    }

    /**
     * Возвращает список названий мероприятий для автодополнения.
     *
     * @param limit максимальное число возвращаемых названий
     * @return список названий
     */
    @GetMapping("/titles")
    public List<String> titles(@RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        return eventService.listTitles(limit);
    }

    /**
     * Регистрирует пользователя на мероприятие.
     *
     * @param id идентификатор мероприятия
     * @param request данные регистрации
     */
    @PostMapping("/{id}/register")
    public void register(@PathVariable("id") Long id, @Valid @RequestBody EventRegistrationRequest request) {
        eventService.register(id, request.userId(), request.count(), request.amount());
    }

    /**
     * Отменяет регистрацию пользователя на мероприятие.
     *
     * @param id идентификатор мероприятия
     * @param request данные отмены регистрации
     */
    @PostMapping("/{id}/unregister")
    public void unregister(@PathVariable("id") Long id, @Valid @RequestBody EventRegistrationRequest request) {
        eventService.unregister(id, request.userId(), request.count(), request.amount());
    }

    /**
     * Подтверждает участие пользователя в мероприятии.
     *
     * @param id идентификатор мероприятия
     * @param request данные подтверждения участия
     */
    @PostMapping("/{id}/confirm")
    public void confirmAttendance(
            @PathVariable("id") Long id,
            @Valid @RequestBody EventAttendanceConfirmRequest request
    ) {
        eventService.confirmAttendance(id, request.userId());
    }

    /**
     * Отмечает, что пользователь не придет на мероприятие.
     *
     * @param id идентификатор мероприятия
     * @param request данные отклонения участия
     */
    @PostMapping("/{id}/decline")
    public void declineAttendance(
            @PathVariable("id") Long id,
            @Valid @RequestBody EventAttendanceConfirmRequest request
    ) {
        eventService.declineAttendance(id, request.userId());
    }
}
