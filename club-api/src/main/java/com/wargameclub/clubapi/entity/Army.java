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
 * JPA-сущность армии пользователя или клубной армии.
 */
@Entity
@Table(name = "army")
public class Army {

    /**
     * Идентификатор армии.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Владелец армии.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    /**
     * Название игры/системы.
     */
    @Column(nullable = false, length = 100)
    private String game;

    /**
     * Фракция или подфракция.
     */
    @Column(nullable = false, length = 100)
    private String faction;

    /**
     * Признак доступности армии для клуба.
     */
    @Column(nullable = false)
    private boolean isClubShared = false;

    /**
     * Признак активности записи.
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
    public Army() {
    }

    /**
     * Создает армию с указанными параметрами.
     *
     * @param owner владелец армии
     * @param game название игры/системы
     * @param faction фракция или подфракция
     * @param isClubShared признак доступности армии для клуба
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
     * Возвращает идентификатор армии.
     *
     * @return идентификатор армии
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает владельца армии.
     *
     * @return владелец армии
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Устанавливает владельца армии.
     *
     * @param owner владелец армии
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /**
     * Возвращает название игры/системы.
     *
     * @return название игры
     */
    public String getGame() {
        return game;
    }

    /**
     * Устанавливает название игры/системы.
     *
     * @param game название игры
     */
    public void setGame(String game) {
        this.game = game;
    }

    /**
     * Возвращает фракцию или подфракцию.
     *
     * @return фракция
     */
    public String getFaction() {
        return faction;
    }

    /**
     * Устанавливает фракцию или подфракцию.
     *
     * @param faction фракция
     */
    public void setFaction(String faction) {
        this.faction = faction;
    }

    /**
     * Проверяет, доступна ли армия для клуба.
     *
     * @return true, если армия клубная
     */
    public boolean isClubShared() {
        return isClubShared;
    }

    /**
     * Устанавливает признак доступности армии для клуба.
     *
     * @param clubShared признак клубной армии
     */
    public void setClubShared(boolean clubShared) {
        isClubShared = clubShared;
    }

    /**
     * Проверяет, активна ли запись армии.
     *
     * @return true, если армия активна
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Устанавливает признак активности записи армии.
     *
     * @param active признак активности
     */
    public void setActive(boolean active) {
        isActive = active;
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
