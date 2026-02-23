package com.wargameclub.clubapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA-сущность стол клуба.
 */
@Entity
@Table(name = "club_table")
public class ClubTable {

    /**
     * Поле состояния.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Поле состояния.
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private boolean isActive = true;

    /**
     * Поле состояния.
     */
    @Column
    private String notes;

    /**
     * Конструктор ClubTable.
     */
    public ClubTable() {
    }

    /**
     * Конструктор ClubTable.
     */
    public ClubTable(String name, boolean isActive, String notes) {
        this.name = name;
        this.isActive = isActive;
        this.notes = notes;
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
     * Возвращает Notes.
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Устанавливает Notes.
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }
}

