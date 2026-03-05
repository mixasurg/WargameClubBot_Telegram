package com.wargameclub.clubapi.lab;

import com.wargameclub.clubapi.lab.dto.LabResilienceStatsDto;
import com.wargameclub.clubapi.lab.service.LabResilienceAdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Диагностические эндпоинты лабораторного контура.
 */
@RestController
@RequestMapping("/api/lab/resilience")
public class LabResilienceController {

    private final LabResilienceAdminService adminService;

    public LabResilienceController(LabResilienceAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public LabResilienceStatsDto stats() {
        return adminService.stats();
    }

    @PostMapping("/reset")
    public LabResilienceStatsDto reset() {
        return adminService.resetState();
    }
}
