package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA-сущность каталог игр.
 */
@Entity
@Table(name = "game_catalog")
public class GameCatalog {

    /**
     * Поле состояния.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Поле состояния.
     */
    @Column(nullable = false, length = 120, unique = true)
    private String name;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private int defaultDurationMinutes;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private int tableUnits;

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
     * Конструктор GameCatalog.
     */
    public GameCatalog() {
    }

    /**
     * Конструктор GameCatalog.
     */
    public GameCatalog(String name, int defaultDurationMinutes, int tableUnits) {
        this.name = name;
        this.defaultDurationMinutes = defaultDurationMinutes;
        this.tableUnits = tableUnits;
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
     * Возвращает Name.
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает Name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает DefaultDurationMinutes.
     */
    public int getDefaultDurationMinutes() {
        return defaultDurationMinutes;
    }

    /**
     * Устанавливает DefaultDurationMinutes.
     */
    public void setDefaultDurationMinutes(int defaultDurationMinutes) {
        this.defaultDurationMinutes = defaultDurationMinutes;
    }

    /**
     * Возвращает количество столов.
     */
    public int getTableUnits() {
        return tableUnits;
    }

    /**
     * Устанавливает количество столов.
     */
    public void setTableUnits(int tableUnits) {
        this.tableUnits = tableUnits;
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

