package com.wargameclub.clubapi.controller;

import com.wargameclub.clubapi.dto.LoyaltyDto;
import com.wargameclub.clubapi.service.LoyaltyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loyalty")
public class LoyaltyController {
    private final LoyaltyService loyaltyService;

    public LoyaltyController(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @GetMapping("/{userId}")
    public LoyaltyDto get(@PathVariable Long userId) {
        return new LoyaltyDto(userId, loyaltyService.getPoints(userId));
    }
}

