package com.wargameclub.clubapi.controller;

import java.time.OffsetDateTime;
import java.util.List;
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

@RestController
@Validated
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;
    private final UserService userService;

    public EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

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

    @PutMapping("/{id}")
    public EventDto update(@PathVariable Long id, @Valid @RequestBody EventUpdateRequest request) {
        return DtoMapper.toEventDto(eventService.update(id, request));
    }

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

    @GetMapping("/titles")
    public List<String> titles(@RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        return eventService.listTitles(limit);
    }

    @PostMapping("/{id}/register")
    public void register(@PathVariable Long id, @Valid @RequestBody EventRegistrationRequest request) {
        eventService.register(id, request.userId(), request.count(), request.amount());
    }

    @PostMapping("/{id}/unregister")
    public void unregister(@PathVariable Long id, @Valid @RequestBody EventRegistrationRequest request) {
        eventService.unregister(id, request.userId(), request.count(), request.amount());
    }
}

