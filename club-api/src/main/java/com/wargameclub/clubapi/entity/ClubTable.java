package com.wargameclub.clubapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA-сущность игрового стола клуба.
 */
@Entity
@Table(name = "club_table")
public class ClubTable {

    /**
     * Идентификатор стола.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название стола.
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * Признак активности стола.
     */
    @Column(nullable = false)
    private boolean isActive = true;

    /**
     * Дополнительные примечания.
     */
    @Column
    private String notes;

    /**
     * Создает пустую сущность для JPA.
     */
    public ClubTable() {
    }

    /**
     * Создает стол с указанными параметрами.
     *
     * @param name название стола
     * @param isActive признак активности
     * @param notes примечания (опционально)
     */
    public ClubTable(String name, boolean isActive, String notes) {
        this.name = name;
        this.isActive = isActive;
        this.notes = notes;
    }

    /**
     * Возвращает идентификатор стола.
     *
     * @return идентификатор стола
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает название стола.
     *
     * @return название стола
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название стола.
     *
     * @param name название стола
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Проверяет, активен ли стол.
     *
     * @return true, если стол активен
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Устанавливает признак активности стола.
     *
     * @param active признак активности
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Возвращает примечания к столу.
     *
     * @return примечания
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Устанавливает примечания к столу.
     *
     * @param notes примечания
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
