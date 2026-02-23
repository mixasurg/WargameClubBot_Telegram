package com.wargameclub.clubapi.controller;

import java.util.List;
import com.wargameclub.clubapi.dto.GameCreateRequest;
import com.wargameclub.clubapi.dto.GameDto;
import com.wargameclub.clubapi.entity.GameCatalog;
import com.wargameclub.clubapi.service.GameCatalogService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для управления играми.
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    /**
     * Сервис каталога игр.
     */
    private final GameCatalogService gameCatalogService;

    /**
     * Конструктор GameController.
     */
    public GameController(GameCatalogService gameCatalogService) {
        this.gameCatalogService = gameCatalogService;
    }

    /**
     * Возвращает список игр.
     */
    @GetMapping
    public List<GameDto> list(@RequestParam(name = "active", required = false) Boolean active) {
        return gameCatalogService.list(active).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Создает игру.
     */
    @PostMapping
    public GameDto create(@Valid @RequestBody GameCreateRequest request) {
        GameCatalog game = gameCatalogService.createOrGet(
                request.name(),
                request.defaultDurationMinutes(),
                request.tableUnits()
        );
        return toDto(game);
    }

    /**
     * Преобразует в DTO.
     */
    private GameDto toDto(GameCatalog game) {
        return new GameDto(
                game.getId(),
                game.getName(),
                game.getDefaultDurationMinutes(),
                game.getTableUnits(),
                game.isActive(),
                game.getCreatedAt()
        );
    }
}

