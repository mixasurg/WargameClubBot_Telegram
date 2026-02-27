package com.wargameclub.clubapi.controller;

import com.wargameclub.clubapi.dto.LoyaltyDto;
import com.wargameclub.clubapi.service.LoyaltyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для управления лояльностью.
 */
@RestController
@RequestMapping("/api/loyalty")
public class LoyaltyController {

    /**
     * Сервис лояльности.
     */
    private final LoyaltyService loyaltyService;

    /**
     * Создает контроллер для работы с баллами лояльности.
     *
     * @param loyaltyService сервис лояльности
     */
    public LoyaltyController(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    /**
     * Возвращает количество баллов лояльности пользователя.
     *
     * @param userId идентификатор пользователя
     * @return DTO с балансом лояльности
     */
    @GetMapping("/{userId}")
    public LoyaltyDto get(@PathVariable Long userId) {
        return new LoyaltyDto(userId, loyaltyService.getPoints(userId));
    }
}
