package com.wargameclub.clubapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "loyalty_account")
public class LoyaltyAccount {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private int points = 0;

    public LoyaltyAccount() {
    }

    public LoyaltyAccount(Long userId, int points) {
        this.userId = userId;
        this.points = points;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}

