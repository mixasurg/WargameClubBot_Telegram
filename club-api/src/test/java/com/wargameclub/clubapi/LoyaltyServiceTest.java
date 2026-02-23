package com.wargameclub.clubapi;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.repository.UserRepository;
import com.wargameclub.clubapi.service.ArmyService;
import com.wargameclub.clubapi.service.LoyaltyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Класс модуля club-api.
 */
@SpringBootTest
@ActiveProfiles("test")
public class LoyaltyServiceTest {

    /**
     * Сервис армии.
     */
    @Autowired
    private ArmyService armyService;

    /**
     * Сервис лояльности.
     */
    @Autowired
    private LoyaltyService loyaltyService;

    /**
     * Репозиторий пользователя.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Выполняет операцию.
     */
    @Test
    void armyUsageAddsPointsToOwner() {
        User owner = userRepository.save(new User("Owner"));
        User usedBy = userRepository.save(new User("Guest"));

        var army = armyService.create(owner.getId(), "Game", "Faction", true);
        armyService.useArmy(army.getId(), usedBy.getId(), OffsetDateTime.now(), "Test");

        /**
         * Выполняет операцию.
         */
        assertEquals(15, loyaltyService.getPoints(owner.getId()));
    }
}

