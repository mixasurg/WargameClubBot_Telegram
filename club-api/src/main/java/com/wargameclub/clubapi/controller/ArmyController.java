package com.wargameclub.clubapi.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.wargameclub.clubapi.dto.ArmyCreateRequest;
import com.wargameclub.clubapi.dto.ArmyDto;
import com.wargameclub.clubapi.dto.ArmyUsageRequest;
import com.wargameclub.clubapi.service.ArmyService;
import com.wargameclub.clubapi.service.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для управления армиями.
 */
@RestController
@RequestMapping("/api/armies")
public class ArmyController {

    /**
     * Сервис армии.
     */
    private final ArmyService armyService;

    /**
     * Создает контроллер для операций над армиями.
     *
     * @param armyService сервис работы с армиями
     */
    public ArmyController(ArmyService armyService) {
        this.armyService = armyService;
    }

    /**
     * Создает армию пользователя.
     *
     * @param request данные для создания армии
     * @return созданная армия
     */
    @PostMapping
    public ArmyDto create(@Valid @RequestBody ArmyCreateRequest request) {
        return DtoMapper.toArmyDto(armyService.create(
                request.ownerUserId(),
                request.game(),
                request.faction(),
                request.isClubShared()
        ));
    }

    /**
     * Возвращает список армий с учетом фильтров.
     *
     * @param game название игры (опционально)
     * @param faction фракция (опционально)
     * @param clubShared признак клубной армии (опционально)
     * @param ownerUserId идентификатор владельца (опционально)
     * @param active признак активности (опционально)
     * @return список армий
     */
    @GetMapping
    public List<ArmyDto> list(
            @RequestParam(name = "game", required = false) String game,
            @RequestParam(name = "faction", required = false) String faction,
            @RequestParam(name = "clubShared", required = false) Boolean clubShared,
            @RequestParam(name = "ownerUserId", required = false) Long ownerUserId,
            @RequestParam(name = "active", required = false) Boolean active
    ) {
        String normalizedGame = decodeQueryValue(game);
        String normalizedFaction = decodeQueryValue(faction);
        return armyService.find(normalizedGame, normalizedFaction, clubShared, ownerUserId, active).stream()
                .map(DtoMapper::toArmyDto)
                .toList();
    }

    /**
     * Декодирует значение параметра запроса, если оно URL-encoded.
     *
     * @param value значение параметра
     * @return декодированное значение или исходное, если декодирование не требуется/не удалось
     */
    private String decodeQueryValue(String value) {
        if (value == null || !value.contains("%")) {
            return value;
        }
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return value;
        }
    }

    /**
     * Деактивирует армию по идентификатору.
     *
     * @param id идентификатор армии
     * @return обновленная армия
     */
    @PostMapping("/{id}/deactivate")
    public ArmyDto deactivate(@PathVariable Long id) {
        return DtoMapper.toArmyDto(armyService.deactivate(id));
    }

    /**
     * Фиксирует использование армии пользователем.
     *
     * @param id идентификатор армии
     * @param request данные об использовании
     */
    @PostMapping("/{id}/use")
    public void use(@PathVariable Long id, @Valid @RequestBody ArmyUsageRequest request) {
        armyService.useArmy(id, request.usedByUserId(), request.usedAt(), request.notes());
    }
}
