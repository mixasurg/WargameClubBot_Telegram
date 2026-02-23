package com.wargameclub.clubapi.controller;

import java.util.List;
import com.wargameclub.clubapi.dto.TableDto;
import com.wargameclub.clubapi.service.DtoMapper;
import com.wargameclub.clubapi.service.TableService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для управления столами.
 */
@RestController
@RequestMapping("/api/tables")
public class TableController {

    /**
     * Сервис стола.
     */
    private final TableService tableService;

    /**
     * Конструктор TableController.
     */
    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    /**
     * Возвращает список столов.
     */
    @GetMapping
    public List<TableDto> list() {
        return tableService.findAll().stream()
                .map(DtoMapper::toTableDto)
                .toList();
    }
}

