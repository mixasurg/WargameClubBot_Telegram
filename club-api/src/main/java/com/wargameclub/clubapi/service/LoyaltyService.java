package com.wargameclub.clubapi.service;

import com.wargameclub.clubapi.config.AppProperties;
import com.wargameclub.clubapi.entity.LoyaltyAccount;
import com.wargameclub.clubapi.repository.LoyaltyAccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис управления баллами лояльности пользователей.
 */
@Service
public class LoyaltyService {

    /**
     * Репозиторий счетов лояльности.
     */
    private final LoyaltyAccountRepository repository;

    /**
     * Настройки приложения.
     */
    private final AppProperties appProperties;

    /**
     * Создает сервис лояльности.
     *
     * @param repository репозиторий счетов
     * @param appProperties настройки приложения
     */
    public LoyaltyService(LoyaltyAccountRepository repository, AppProperties appProperties) {
        this.repository = repository;
        this.appProperties = appProperties;
    }

    /**
     * Начисляет баллы за использование армии.
     *
     * @param userId идентификатор пользователя
     * @return новый баланс баллов
     */
    @Transactional
    public int addPoints(Long userId) {
        int pointsToAdd = appProperties.getLoyalty().getPointsArmyUsed();
        return addPoints(userId, pointsToAdd);
    }

    /**
     * Начисляет баллы за шаринг армии.
     *
     * @param userId идентификатор пользователя
     * @return новый баланс баллов
     */
    @Transactional
    public int addPointsForSharedArmy(Long userId) {
        int pointsToAdd = appProperties.getLoyalty().getPointsArmyShared();
        return addPoints(userId, pointsToAdd);
    }

    /**
     * Увеличивает баланс лояльности на указанное количество баллов.
     *
     * @param userId идентификатор пользователя
     * @param pointsToAdd число начисляемых баллов
     * @return новый баланс
     */
    private int addPoints(Long userId, int pointsToAdd) {
        LoyaltyAccount account = repository.findByIdForUpdate(userId).orElse(null);
        if (account == null) {
            try {
                LoyaltyAccount created = repository.saveAndFlush(new LoyaltyAccount(userId, pointsToAdd));
                return created.getPoints();
            } catch (DataIntegrityViolationException ex) {
                account = repository.findByIdForUpdate(userId).orElseThrow(() -> ex);
            }
        }
        account.setPoints(account.getPoints() + pointsToAdd);
        return account.getPoints();
    }

    /**
     * Возвращает текущий баланс баллов пользователя.
     *
     * @param userId идентификатор пользователя
     * @return баланс баллов
     */
    @Transactional(readOnly = true)
    public int getPoints(Long userId) {
        return repository.findById(userId)
                .map(LoyaltyAccount::getPoints)
                .orElse(0);
    }
}
