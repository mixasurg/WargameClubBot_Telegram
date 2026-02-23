package com.wargameclub.clubapi.service;

import com.wargameclub.clubapi.config.AppProperties;
import com.wargameclub.clubapi.entity.LoyaltyAccount;
import com.wargameclub.clubapi.repository.LoyaltyAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для работы с лояльностью.
 */
@Service
public class LoyaltyService {

    /**
     * Репозиторий LoyaltyAccount.
     */
    private final LoyaltyAccountRepository repository;

    /**
     * Параметры конфигурации App.
     */
    private final AppProperties appProperties;

    /**
     * Конструктор LoyaltyService.
     */
    public LoyaltyService(LoyaltyAccountRepository repository, AppProperties appProperties) {
        this.repository = repository;
        this.appProperties = appProperties;
    }

    /**
     * Добавляет Points.
     */
    @Transactional
    public int addPoints(Long userId) {
        int pointsToAdd = appProperties.getLoyalty().getPointsArmyUsed();
        return addPoints(userId, pointsToAdd);
    }

    /**
     * Добавляет PointsForSharedArmy.
     */
    @Transactional
    public int addPointsForSharedArmy(Long userId) {
        int pointsToAdd = appProperties.getLoyalty().getPointsArmyShared();
        return addPoints(userId, pointsToAdd);
    }

    /**
     * Добавляет Points.
     */
    private int addPoints(Long userId, int pointsToAdd) {
        LoyaltyAccount account = repository.findById(userId)
                .orElseGet(() -> new LoyaltyAccount(userId, 0));
        account.setPoints(account.getPoints() + pointsToAdd);
        repository.save(account);
        return account.getPoints();
    }

    /**
     * Возвращает Points.
     */
    @Transactional(readOnly = true)
    public int getPoints(Long userId) {
        return repository.findById(userId)
                .map(LoyaltyAccount::getPoints)
                .orElse(0);
    }
}

