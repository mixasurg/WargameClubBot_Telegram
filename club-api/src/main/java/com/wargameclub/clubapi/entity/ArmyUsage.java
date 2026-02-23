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
 * JPA-сущность использование армии.
 */
@Entity
@Table(name = "army_usage")
public class ArmyUsage {

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
    @JoinColumn(name = "army_id", nullable = false)
    private Army army;

    /**
     * Поле состояния.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_by_user_id", nullable = false)
    private User usedBy;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private OffsetDateTime usedAt;

    /**
     * Поле состояния.
     */
    @Column
    private String notes;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Конструктор ArmyUsage.
     */
    public ArmyUsage() {
    }

    /**
     * Конструктор ArmyUsage.
     */
    public ArmyUsage(Army army, User usedBy, OffsetDateTime usedAt, String notes) {
        this.army = army;
        this.usedBy = usedBy;
        this.usedAt = usedAt;
        this.notes = notes;
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * Возвращает идентификатор.
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает армию.
     */
    public Army getArmy() {
        return army;
    }

    /**
     * Устанавливает армию.
     */
    public void setArmy(Army army) {
        this.army = army;
    }

    /**
     * Возвращает UsedBy.
     */
    public User getUsedBy() {
        return usedBy;
    }

    /**
     * Устанавливает UsedBy.
     */
    public void setUsedBy(User usedBy) {
        this.usedBy = usedBy;
    }

    /**
     * Возвращает UsedAt.
     */
    public OffsetDateTime getUsedAt() {
        return usedAt;
    }

    /**
     * Устанавливает UsedAt.
     */
    public void setUsedAt(OffsetDateTime usedAt) {
        this.usedAt = usedAt;
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

