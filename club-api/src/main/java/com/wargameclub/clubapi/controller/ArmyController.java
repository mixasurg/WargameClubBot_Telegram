package com.wargameclub.clubapi.controller;

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

@RestController
@RequestMapping("/api/armies")
public class ArmyController {
    private final ArmyService armyService;

    public ArmyController(ArmyService armyService) {
        this.armyService = armyService;
    }

    @PostMapping
    public ArmyDto create(@Valid @RequestBody ArmyCreateRequest request) {
        return DtoMapper.toArmyDto(armyService.create(
                request.ownerUserId(),
                request.game(),
                request.faction(),
                request.isClubShared()
        ));
    }

    @GetMapping
    public List<ArmyDto> list(
            @RequestParam(name = "game", required = false) String game,
            @RequestParam(name = "faction", required = false) String faction,
            @RequestParam(name = "clubShared", required = false) Boolean clubShared,
            @RequestParam(name = "active", required = false) Boolean active
    ) {
        return armyService.find(game, faction, clubShared, active).stream()
                .map(DtoMapper::toArmyDto)
                .toList();
    }

    @PostMapping("/{id}/deactivate")
    public ArmyDto deactivate(@PathVariable Long id) {
        return DtoMapper.toArmyDto(armyService.deactivate(id));
    }

    @PostMapping("/{id}/use")
    public void use(@PathVariable Long id, @Valid @RequestBody ArmyUsageRequest request) {
        armyService.useArmy(id, request.usedByUserId(), request.usedAt(), request.notes());
    }
}

