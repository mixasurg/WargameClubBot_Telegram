package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA-сущность записи каталога игр.
 */
@Entity
@Table(name = "game_catalog")
public class GameCatalog {

    /**
     * Идентификатор игры в каталоге.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название игры/системы.
     */
    @Column(nullable = false, length = 120, unique = true)
    private String name;

    /**
     * Длительность игры по умолчанию в минутах.
     */
    @Column(nullable = false)
    private int defaultDurationMinutes;

    /**
     * Требуемое количество единиц стола.
     */
    @Column(nullable = false)
    private int tableUnits;

    /**
     * Признак активности записи в каталоге.
     */
    @Column(nullable = false)
    private boolean isActive = true;

    /**
     * Дата и время создания записи.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Создает пустую сущность для JPA.
     */
    public GameCatalog() {
    }

    /**
     * Создает запись игры в каталоге.
     *
     * @param name название игры/системы
     * @param defaultDurationMinutes длительность по умолчанию
     * @param tableUnits требуемое количество единиц стола
     */
    public GameCatalog(String name, int defaultDurationMinutes, int tableUnits) {
        this.name = name;
        this.defaultDurationMinutes = defaultDurationMinutes;
        this.tableUnits = tableUnits;
        this.isActive = true;
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * Возвращает идентификатор игры.
     *
     * @return идентификатор игры
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает название игры/системы.
     *
     * @return название игры
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название игры/системы.
     *
     * @param name название игры
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает длительность игры по умолчанию.
     *
     * @return длительность в минутах
     */
    public int getDefaultDurationMinutes() {
        return defaultDurationMinutes;
    }

    /**
     * Устанавливает длительность игры по умолчанию.
     *
     * @param defaultDurationMinutes длительность в минутах
     */
    public void setDefaultDurationMinutes(int defaultDurationMinutes) {
        this.defaultDurationMinutes = defaultDurationMinutes;
    }

    /**
     * Возвращает требуемое количество единиц стола.
     *
     * @return количество единиц стола
     */
    public int getTableUnits() {
        return tableUnits;
    }

    /**
     * Устанавливает требуемое количество единиц стола.
     *
     * @param tableUnits количество единиц стола
     */
    public void setTableUnits(int tableUnits) {
        this.tableUnits = tableUnits;
    }

    /**
     * Проверяет, активна ли запись в каталоге.
     *
     * @return true, если игра активна
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Устанавливает признак активности записи в каталоге.
     *
     * @param active признак активности
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Возвращает дату и время создания записи.
     *
     * @return дата и время создания
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Устанавливает дату и время создания записи.
     *
     * @param createdAt дата и время создания
     */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
