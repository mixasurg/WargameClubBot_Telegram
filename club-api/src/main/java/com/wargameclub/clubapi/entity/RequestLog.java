package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA-сущность RequestLog.
 */
@Entity
@Table(name = "request_log")
public class RequestLog {

    /**
     * Поле состояния.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Поле состояния.
     */
    @Column(nullable = false, length = 10)
    private String method;

    /**
     * Поле состояния.
     */
    @Column(nullable = false, length = 300)
    private String path;

    /**
     * Поле состояния.
     */
    @Column(length = 500)
    private String query;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private int status;

    /**
     * Поле состояния.
     */
    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    /**
     * Поле состояния.
     */
    @Column(name = "remote_addr", length = 100)
    private String remoteAddr;

    /**
     * Поле состояния.
     */
    @Column(name = "user_agent", length = 300)
    private String userAgent;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Конструктор RequestLog.
     */
    public RequestLog() {
    }

    /**
     * Выполняет операцию.
     */
    public RequestLog(
            String method,
            String path,
            String query,
            int status,
            long durationMs,
            String remoteAddr,
            String userAgent
    ) {
        this.method = method;
        this.path = path;
        this.query = query;
        this.status = status;
        this.durationMs = durationMs;
        this.remoteAddr = remoteAddr;
        this.userAgent = userAgent;
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * Возвращает идентификатор.
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает Method.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Устанавливает Method.
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Возвращает Path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Устанавливает Path.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Возвращает Query.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Устанавливает Query.
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Возвращает Status.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Устанавливает Status.
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Возвращает DurationMs.
     */
    public long getDurationMs() {
        return durationMs;
    }

    /**
     * Устанавливает DurationMs.
     */
    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    /**
     * Возвращает RemoteAddr.
     */
    public String getRemoteAddr() {
        return remoteAddr;
    }

    /**
     * Устанавливает RemoteAddr.
     */
    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    /**
     * Возвращает UserAgent.
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Устанавливает UserAgent.
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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
