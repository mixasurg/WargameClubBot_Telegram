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
 * JPA-сущность статистики игр пользователя.
 */
@Entity
@Table(name = "user_game_stats")
public class UserGameStats {

    /**
     * Идентификатор пользователя (также первичный ключ статистики).
     */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /**
     * Пользователь, к которому относится статистика.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Количество побед.
     */
    @Column(nullable = false)
    private int wins = 0;

    /**
     * Количество поражений.
     */
    @Column(nullable = false)
    private int losses = 0;

    /**
     * Количество ничьих.
     */
    @Column(nullable = false)
    private int draws = 0;

    /**
     * Дата и время последнего обновления.
     */
    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    /**
     * Создает пустую сущность для JPA.
     */
    public UserGameStats() {
    }

    /**
     * Создает статистику для указанного пользователя.
     *
     * @param user пользователь
     */
    public UserGameStats(User user) {
        this.user = user;
        this.userId = user != null ? user.getId() : null;
        this.updatedAt = OffsetDateTime.now();
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
     * Возвращает пользователя.
     *
     * @return пользователь
     */
    public User getUser() {
        return user;
    }

    /**
     * Возвращает количество побед.
     *
     * @return количество побед
     */
    public int getWins() {
        return wins;
    }

    /**
     * Устанавливает количество побед.
     *
     * @param wins количество побед
     */
    public void setWins(int wins) {
        this.wins = wins;
    }

    /**
     * Возвращает количество поражений.
     *
     * @return количество поражений
     */
    public int getLosses() {
        return losses;
    }

    /**
     * Устанавливает количество поражений.
     *
     * @param losses количество поражений
     */
    public void setLosses(int losses) {
        this.losses = losses;
    }

    /**
     * Возвращает количество ничьих.
     *
     * @return количество ничьих
     */
    public int getDraws() {
        return draws;
    }

    /**
     * Устанавливает количество ничьих.
     *
     * @param draws количество ничьих
     */
    public void setDraws(int draws) {
        this.draws = draws;
    }

    /**
     * Возвращает дату и время последнего обновления.
     *
     * @return дата и время обновления
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Устанавливает дату и время последнего обновления.
     *
     * @param updatedAt дата и время обновления
     */
    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
