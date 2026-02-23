package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA-сущность пользователь.
 */
@Entity
@Table(name = "app_user")
public class User {

    /**
     * Поле состояния.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Поле состояния.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Поле состояния.
     */
    @Column(name = "telegram_id", unique = true)
    private Long telegramId;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Конструктор User.
     */
    public User() {
    }

    /**
     * Конструктор User.
     */
    public User(String name) {
        this.name = name;
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * Конструктор User.
     */
    public User(String name, Long telegramId) {
        this.name = name;
        this.telegramId = telegramId;
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
     * Возвращает идентификатор Telegram.
     */
    public Long getTelegramId() {
        return telegramId;
    }

    /**
     * Устанавливает идентификатор Telegram.
     */
    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
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

