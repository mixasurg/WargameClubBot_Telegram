package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
     * Логин пользователя для авторизации.
     */
    @Column(length = 120, unique = true)
    private String login;

    /**
     * Хеш пароля пользователя.
     */
    @Column(name = "password_hash", length = 120)
    private String passwordHash;

    /**
     * Роль пользователя для RBAC-авторизации.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.MEMBER;

    /**
     * Признак активного аккаунта.
     */
    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = true;

    /**
     * Дата и время последнего успешного входа.
     */
    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

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
        this.role = UserRole.MEMBER;
        this.enabled = true;
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
        this.role = UserRole.MEMBER;
        this.enabled = true;
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
     * Возвращает логин пользователя.
     *
     * @return логин или null
     */
    public String getLogin() {
        return login;
    }

    /**
     * Устанавливает логин пользователя.
     *
     * @param login логин
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Возвращает хеш пароля пользователя.
     *
     * @return хеш пароля или null
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Устанавливает хеш пароля пользователя.
     *
     * @param passwordHash хеш пароля
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Возвращает роль пользователя.
     *
     * @return роль пользователя
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Устанавливает роль пользователя.
     *
     * @param role роль пользователя
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Возвращает признак активности аккаунта.
     *
     * @return true, если аккаунт активен
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Устанавливает признак активности аккаунта.
     *
     * @param enabled признак активности
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Возвращает время последнего входа пользователя.
     *
     * @return время последнего входа или null
     */
    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * Устанавливает время последнего входа пользователя.
     *
     * @param lastLoginAt время последнего входа
     */
    public void setLastLoginAt(OffsetDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
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
