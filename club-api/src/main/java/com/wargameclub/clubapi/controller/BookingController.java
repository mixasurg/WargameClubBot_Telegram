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

/**
 * REST-контроллер для управления бронированиями.
 */
@RestController
@Validated
@RequestMapping("/api/bookings")
public class BookingController {

    /**
     * Сервис бронирования.
     */
    private final BookingService bookingService;

    /**
     * Сервис результата игры.
     */
    private final GameResultService resultService;

    /**
     * Создает контроллер для операций с бронированиями.
     *
     * @param bookingService сервис бронирований
     * @param resultService сервис фиксации результатов игр
     */
    public BookingController(BookingService bookingService, GameResultService resultService) {
        this.bookingService = bookingService;
        this.resultService = resultService;
    }

    /**
     * Создает бронирование стола.
     *
     * @param request данные для создания бронирования
     * @return созданное бронирование
     */
    @PostMapping
    public BookingDto create(@Valid @RequestBody BookingCreateRequest request) {
        return DtoMapper.toBookingDto(bookingService.create(request));
    }

    /**
     * Возвращает список бронирований, пересекающих заданный интервал времени.
     *
     * @param from начало интервала
     * @param to конец интервала
     * @param tableId идентификатор стола для фильтрации (опционально)
     * @return список бронирований
     */
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

    /**
     * Отменяет бронирование по идентификатору.
     *
     * @param id идентификатор бронирования
     * @return отмененное бронирование
     */
    @PostMapping("/{id}/cancel")
    public BookingDto cancel(@PathVariable Long id) {
        return DtoMapper.toBookingDto(bookingService.cancel(id));
    }

    /**
     * Фиксирует результат игры для бронирования.
     *
     * @param id идентификатор бронирования
     * @param request данные о результате
     */
    @PostMapping("/{id}/result")
    public void result(@PathVariable Long id, @Valid @RequestBody BookingResultRequest request) {
        resultService.recordResult(id, request.reporterUserId(), request.outcome());
    }
}
