package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * JPA-сущность статистика игрока.
 */
@Entity
@Table(name = "user_game_stats")
public class UserGameStats {

    /**
     * Поле состояния.
     */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /**
     * Поле состояния.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private int wins = 0;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private int losses = 0;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private int draws = 0;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    /**
     * Конструктор UserGameStats.
     */
    public UserGameStats() {
    }

    /**
     * Конструктор UserGameStats.
     */
    public UserGameStats(User user) {
        this.user = user;
        this.userId = user != null ? user.getId() : null;
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Возвращает идентификатор пользователя.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Возвращает пользователя.
     */
    public User getUser() {
        return user;
    }

    /**
     * Возвращает Wins.
     */
    public int getWins() {
        return wins;
    }

    /**
     * Устанавливает Wins.
     */
    public void setWins(int wins) {
        this.wins = wins;
    }

    /**
     * Возвращает Losses.
     */
    public int getLosses() {
        return losses;
    }

    /**
     * Устанавливает Losses.
     */
    public void setLosses(int losses) {
        this.losses = losses;
    }

    /**
     * Возвращает Draws.
     */
    public int getDraws() {
        return draws;
    }

    /**
     * Устанавливает Draws.
     */
    public void setDraws(int draws) {
        this.draws = draws;
    }

    /**
     * Возвращает UpdatedAt.
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Устанавливает UpdatedAt.
     */
    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
