package com.wargameclub.clubapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA-сущность LoyaltyAccount.
 */
@Entity
@Table(name = "loyalty_account")
public class LoyaltyAccount {

    /**
     * Поле состояния.
     */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private int points = 0;

    /**
     * Конструктор LoyaltyAccount.
     */
    public LoyaltyAccount() {
    }

    /**
     * Конструктор LoyaltyAccount.
     */
    public LoyaltyAccount(Long userId, int points) {
        this.userId = userId;
        this.points = points;
    }

    /**
     * Возвращает идентификатор пользователя.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Устанавливает идентификатор пользователя.
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Возвращает Points.
     */
    public int getPoints() {
        return points;
    }

    /**
     * Устанавливает Points.
     */
    public void setPoints(int points) {
        this.points = points;
    }
}

