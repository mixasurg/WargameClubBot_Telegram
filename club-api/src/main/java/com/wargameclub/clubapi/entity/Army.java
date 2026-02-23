package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA-сущность армия.
 */
@Entity
@Table(name = "army")
public class Army {

    /**
     * Поле состояния.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Поле состояния.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    /**
     * Поле состояния.
     */
    @Column(nullable = false, length = 100)
    private String game;

    /**
     * Поле состояния.
     */
    @Column(nullable = false, length = 100)
    private String faction;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private boolean isClubShared = false;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private boolean isActive = true;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Конструктор Army.
     */
    public Army() {
    }

    /**
     * Конструктор Army.
     */
    public Army(User owner, String game, String faction, boolean isClubShared) {
        this.owner = owner;
        this.game = game;
        this.faction = faction;
        this.isClubShared = isClubShared;
        this.isActive = true;
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * Возвращает идентификатор.
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает Owner.
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Устанавливает Owner.
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /**
     * Возвращает игру.
     */
    public String getGame() {
        return game;
    }

    /**
     * Устанавливает игру.
     */
    public void setGame(String game) {
        this.game = game;
    }

    /**
     * Возвращает фракцию.
     */
    public String getFaction() {
        return faction;
    }

    /**
     * Устанавливает фракцию.
     */
    public void setFaction(String faction) {
        this.faction = faction;
    }

    /**
     * Проверяет ClubShared.
     */
    public boolean isClubShared() {
        return isClubShared;
    }

    /**
     * Устанавливает ClubShared.
     */
    public void setClubShared(boolean clubShared) {
        isClubShared = clubShared;
    }

    /**
     * Проверяет Active.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Устанавливает Active.
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Возвращает CreatedAt.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Устанавливает CreatedAt.
     */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

