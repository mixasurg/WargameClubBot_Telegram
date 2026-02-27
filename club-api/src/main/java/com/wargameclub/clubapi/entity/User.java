package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA-сущность пользователя клуба.
 */
@Entity
@Table(name = "app_user")
public class User {

    /**
     * Идентификатор пользователя.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Имя пользователя.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Идентификатор пользователя в Telegram (опционально).
     */
    @Column(name = "telegram_id", unique = true)
    private Long telegramId;

    /**
     * Дата и время регистрации.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Создает пустую сущность для JPA.
     */
    public User() {
    }

    /**
     * Создает пользователя с указанным именем.
     *
     * @param name имя пользователя
     */
    public User(String name) {
        this.name = name;
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * Создает пользователя с именем и Telegram ID.
     *
     * @param name имя пользователя
     * @param telegramId идентификатор пользователя в Telegram
     */
    public User(String name, Long telegramId) {
        this.name = name;
        this.telegramId = telegramId;
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * Возвращает идентификатор пользователя.
     *
     * @return идентификатор пользователя
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает имя пользователя.
     *
     * @return имя пользователя
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает имя пользователя.
     *
     * @param name имя пользователя
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает идентификатор пользователя в Telegram.
     *
     * @return идентификатор Telegram или null
     */
    public Long getTelegramId() {
        return telegramId;
    }

    /**
     * Устанавливает идентификатор пользователя в Telegram.
     *
     * @param telegramId идентификатор Telegram
     */
    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }

    /**
     * Возвращает дату и время регистрации.
     *
     * @return дата и время регистрации
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Устанавливает дату и время регистрации.
     *
     * @param createdAt дата и время регистрации
     */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
