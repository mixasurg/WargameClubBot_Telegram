package com.wargameclub.clubapi.controller;

import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubapi.dto.BookingCreateRequest;
import com.wargameclub.clubapi.dto.BookingDto;
import com.wargameclub.clubapi.dto.BookingResultRequest;
import com.wargameclub.clubapi.service.BookingService;
import com.wargameclub.clubapi.service.DtoMapper;
import com.wargameclub.clubapi.service.GameResultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final GameResultService resultService;

    public BookingController(BookingService bookingService, GameResultService resultService) {
        this.bookingService = bookingService;
        this.resultService = resultService;
    }

    @PostMapping
    public BookingDto create(@Valid @RequestBody BookingCreateRequest request) {
        return DtoMapper.toBookingDto(bookingService.create(request));
    }

    @GetMapping
    public List<BookingDto> list(
            @RequestParam(name = "from") @NotNull OffsetDateTime from,
            @RequestParam(name = "to") @NotNull OffsetDateTime to,
            @RequestParam(name = "tableId", required = false) Long tableId
    ) {
        return bookingService.findOverlapping(from, to, tableId).stream()
                .map(DtoMapper::toBookingDto)
                .toList();
    }

    @PostMapping("/{id}/cancel")
    public BookingDto cancel(@PathVariable Long id) {
        return DtoMapper.toBookingDto(bookingService.cancel(id));
    }

    @PostMapping("/{id}/result")
    public void result(@PathVariable Long id, @Valid @RequestBody BookingResultRequest request) {
        resultService.recordResult(id, request.reporterUserId(), request.outcome());
    }
}

