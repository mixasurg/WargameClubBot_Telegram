package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA-сущность записи аудита действий пользователя.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    /**
     * Идентификатор записи аудита.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Идентификатор пользователя, выполнившего действие.
     */
    @Column(name = "actor_user_id")
    private Long actorUserId;

    /**
     * Логин пользователя, выполнившего действие.
     */
    @Column(name = "actor_login", length = 120)
    private String actorLogin;

    /**
     * Роль пользователя на момент действия.
     */
    @Column(name = "actor_role", length = 20)
    private String actorRole;

    /**
     * Имя действия (например, AUTH_LOGIN_SUCCESS).
     */
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    /**
     * HTTP-метод запроса.
     */
    @Column(name = "http_method", length = 10)
    private String httpMethod;

    /**
     * Путь HTTP-запроса.
     */
    @Column(name = "path", length = 255)
    private String path;

    /**
     * Строка запроса.
     */
    @Column(name = "query", length = 500)
    private String query;

    /**
     * HTTP-статус обработки запроса.
     */
    @Column(name = "status")
    private Integer status;

    /**
     * IP-адрес клиента.
     */
    @Column(name = "client_ip", length = 64)
    private String clientIp;

    /**
     * User-Agent клиента.
     */
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    /**
     * Дополнительные детали аудита.
     */
    @Column(name = "details", length = 1000)
    private String details;

    /**
     * Дата и время действия.
     */
    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt = OffsetDateTime.now();

    /**
     * Создает пустую сущность для JPA.
     */
    public AuditLog() {
    }

    /**
     * Возвращает идентификатор записи.
     *
     * @return идентификатор записи
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает идентификатор пользователя.
     *
     * @return идентификатор пользователя
     */
    public Long getActorUserId() {
        return actorUserId;
    }

    /**
     * Устанавливает идентификатор пользователя.
     *
     * @param actorUserId идентификатор пользователя
     */
    public void setActorUserId(Long actorUserId) {
        this.actorUserId = actorUserId;
    }

    /**
     * Возвращает логин пользователя.
     *
     * @return логин пользователя
     */
    public String getActorLogin() {
        return actorLogin;
    }

    /**
     * Устанавливает логин пользователя.
     *
     * @param actorLogin логин пользователя
     */
    public void setActorLogin(String actorLogin) {
        this.actorLogin = actorLogin;
    }

    /**
     * Возвращает роль пользователя.
     *
     * @return роль пользователя
     */
    public String getActorRole() {
        return actorRole;
    }

    /**
     * Устанавливает роль пользователя.
     *
     * @param actorRole роль пользователя
     */
    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    /**
     * Возвращает имя действия.
     *
     * @return имя действия
     */
    public String getAction() {
        return action;
    }

    /**
     * Устанавливает имя действия.
     *
     * @param action имя действия
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Возвращает HTTP-метод.
     *
     * @return HTTP-метод
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * Устанавливает HTTP-метод.
     *
     * @param httpMethod HTTP-метод
     */
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * Возвращает путь запроса.
     *
     * @return путь запроса
     */
    public String getPath() {
        return path;
    }

    /**
     * Устанавливает путь запроса.
     *
     * @param path путь запроса
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Возвращает строку запроса.
     *
     * @return строка запроса
     */
    public String getQuery() {
        return query;
    }

    /**
     * Устанавливает строку запроса.
     *
     * @param query строка запроса
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Возвращает HTTP-статус.
     *
     * @return HTTP-статус
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * Устанавливает HTTP-статус.
     *
     * @param status HTTP-статус
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * Возвращает IP-адрес клиента.
     *
     * @return IP-адрес клиента
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * Устанавливает IP-адрес клиента.
     *
     * @param clientIp IP-адрес клиента
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    /**
     * Возвращает User-Agent клиента.
     *
     * @return User-Agent клиента
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Устанавливает User-Agent клиента.
     *
     * @param userAgent User-Agent клиента
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Возвращает дополнительные детали.
     *
     * @return дополнительные детали
     */
    public String getDetails() {
        return details;
    }

    /**
     * Устанавливает дополнительные детали.
     *
     * @param details дополнительные детали
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * Возвращает дату и время действия.
     *
     * @return дата и время действия
     */
    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    /**
     * Устанавливает дату и время действия.
     *
     * @param occurredAt дата и время действия
     */
    public void setOccurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
