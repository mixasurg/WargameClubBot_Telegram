package com.wargameclub.clubapi.service;

import com.wargameclub.clubapi.config.AppProperties;
import com.wargameclub.clubapi.entity.LoyaltyAccount;
import com.wargameclub.clubapi.repository.LoyaltyAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoyaltyService {
    private final LoyaltyAccountRepository repository;
    private final AppProperties appProperties;

    public LoyaltyService(LoyaltyAccountRepository repository, AppProperties appProperties) {
        this.repository = repository;
        this.appProperties = appProperties;
    }

    @Transactional
    public int addPoints(Long userId) {
        int pointsToAdd = appProperties.getLoyalty().getPointsArmyUsed();
        return addPoints(userId, pointsToAdd);
    }

    @Transactional
    public int addPointsForSharedArmy(Long userId) {
        int pointsToAdd = appProperties.getLoyalty().getPointsArmyShared();
        return addPoints(userId, pointsToAdd);
    }

    private int addPoints(Long userId, int pointsToAdd) {
        LoyaltyAccount account = repository.findById(userId)
                .orElseGet(() -> new LoyaltyAccount(userId, 0));
        account.setPoints(account.getPoints() + pointsToAdd);
        repository.save(account);
        return account.getPoints();
    }

    @Transactional(readOnly = true)
    public int getPoints(Long userId) {
        return repository.findById(userId)
                .map(LoyaltyAccount::getPoints)
                .orElse(0);
    }
}

