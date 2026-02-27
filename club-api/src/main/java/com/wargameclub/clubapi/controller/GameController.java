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
     * Создает контроллер для операций с каталогом игр.
     *
     * @param gameCatalogService сервис каталога игр
     */
    public GameController(GameCatalogService gameCatalogService) {
        this.gameCatalogService = gameCatalogService;
    }

    /**
     * Возвращает список игр с учетом фильтра активности.
     *
     * @param active фильтр по активности (опционально)
     * @return список игр
     */
    @GetMapping
    public List<GameDto> list(@RequestParam(name = "active", required = false) Boolean active) {
        return gameCatalogService.list(active).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Создает новую игру или возвращает существующую с тем же названием.
     *
     * @param request данные для создания игры
     * @return созданная или найденная игра
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
     * Преобразует сущность игры в DTO.
     *
     * @param game сущность игры
     * @return DTO игры
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
