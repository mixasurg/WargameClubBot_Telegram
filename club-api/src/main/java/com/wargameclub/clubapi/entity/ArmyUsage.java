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
 * JPA-сущность факта использования армии пользователем.
 */
@Entity
@Table(name = "army_usage")
public class ArmyUsage {

    /**
     * Идентификатор записи использования.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Использованная армия.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "army_id", nullable = false)
    private Army army;

    /**
     * Пользователь, использовавший армию.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_by_user_id", nullable = false)
    private User usedBy;

    /**
     * Дата и время использования.
     */
    @Column(nullable = false)
    private OffsetDateTime usedAt;

    /**
     * Примечание к использованию (опционально).
     */
    @Column
    private String notes;

    /**
     * Дата и время создания записи.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Создает пустую сущность для JPA.
     */
    public ArmyUsage() {
    }

    /**
     * Создает запись использования армии.
     *
     * @param army использованная армия
     * @param usedBy пользователь, использовавший армию
     * @param usedAt дата и время использования
     * @param notes примечание (опционально)
     */
    public ArmyUsage(Army army, User usedBy, OffsetDateTime usedAt, String notes) {
        this.army = army;
        this.usedBy = usedBy;
        this.usedAt = usedAt;
        this.notes = notes;
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * Возвращает идентификатор записи использования.
     *
     * @return идентификатор записи
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает использованную армию.
     *
     * @return армия
     */
    public Army getArmy() {
        return army;
    }

    /**
     * Устанавливает использованную армию.
     *
     * @param army армия
     */
    public void setArmy(Army army) {
        this.army = army;
    }

    /**
     * Возвращает пользователя, использовавшего армию.
     *
     * @return пользователь
     */
    public User getUsedBy() {
        return usedBy;
    }

    /**
     * Устанавливает пользователя, использовавшего армию.
     *
     * @param usedBy пользователь
     */
    public void setUsedBy(User usedBy) {
        this.usedBy = usedBy;
    }

    /**
     * Возвращает дату и время использования.
     *
     * @return дата и время использования
     */
    public OffsetDateTime getUsedAt() {
        return usedAt;
    }

    /**
     * Устанавливает дату и время использования.
     *
     * @param usedAt дата и время использования
     */
    public void setUsedAt(OffsetDateTime usedAt) {
        this.usedAt = usedAt;
    }

    /**
     * Возвращает примечание к использованию.
     *
     * @return примечание
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Устанавливает примечание к использованию.
     *
     * @param notes примечание
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Возвращает дату и время создания записи.
     *
     * @return дата создания
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Устанавливает дату и время создания записи.
     *
     * @param createdAt дата создания
     */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
