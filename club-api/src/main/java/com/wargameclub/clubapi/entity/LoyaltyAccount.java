package com.wargameclub.clubapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA-сущность счета лояльности пользователя.
 */
@Entity
@Table(name = "loyalty_account")
public class LoyaltyAccount {

    /**
     * Идентификатор пользователя, которому принадлежит счет.
     */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /**
     * Текущий баланс баллов лояльности.
     */
    @Column(nullable = false)
    private int points = 0;

    /**
     * Создает пустую сущность для JPA.
     */
    public LoyaltyAccount() {
    }

    /**
     * Создает счет лояльности с указанным балансом.
     *
     * @param userId идентификатор пользователя
     * @param points количество баллов
     */
    public LoyaltyAccount(Long userId, int points) {
        this.userId = userId;
        this.points = points;
    }

    /**
     * Возвращает идентификатор пользователя.
     *
     * @return идентификатор пользователя
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Устанавливает идентификатор пользователя.
     *
     * @param userId идентификатор пользователя
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Возвращает количество баллов лояльности.
     *
     * @return количество баллов
     */
    public int getPoints() {
        return points;
    }

    /**
     * Устанавливает количество баллов лояльности.
     *
     * @param points количество баллов
     */
    public void setPoints(int points) {
        this.points = points;
    }
}
