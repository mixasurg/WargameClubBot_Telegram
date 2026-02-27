package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA-сущность записи лога HTTP-запроса.
 */
@Entity
@Table(name = "request_log")
public class RequestLog {

    /**
     * Идентификатор записи лога.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * HTTP-метод запроса.
     */
    @Column(nullable = false, length = 10)
    private String method;

    /**
     * Путь запроса.
     */
    @Column(nullable = false, length = 300)
    private String path;

    /**
     * Строка запроса (query string).
     */
    @Column(length = 500)
    private String query;

    /**
     * HTTP-статус ответа.
     */
    @Column(nullable = false)
    private int status;

    /**
     * Длительность обработки запроса в миллисекундах.
     */
    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    /**
     * IP-адрес клиента (remote address).
     */
    @Column(name = "remote_addr", length = 100)
    private String remoteAddr;

    /**
     * Заголовок User-Agent.
     */
    @Column(name = "user_agent", length = 300)
    private String userAgent;

    /**
     * Дата и время создания записи.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Создает пустую сущность для JPA.
     */
    public RequestLog() {
    }

    /**
     * Создает запись лога запроса с заданными параметрами.
     *
     * @param method HTTP-метод
     * @param path путь запроса
     * @param query строка запроса
     * @param status HTTP-статус ответа
     * @param durationMs длительность обработки в миллисекундах
     * @param remoteAddr IP-адрес клиента
     * @param userAgent заголовок User-Agent
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
     * Возвращает идентификатор записи лога.
     *
     * @return идентификатор записи
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает HTTP-метод запроса.
     *
     * @return HTTP-метод
     */
    public String getMethod() {
        return method;
    }

    /**
     * Устанавливает HTTP-метод запроса.
     *
     * @param method HTTP-метод
     */
    public void setMethod(String method) {
        this.method = method;
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
     * Возвращает HTTP-статус ответа.
     *
     * @return HTTP-статус
     */
    public int getStatus() {
        return status;
    }

    /**
     * Устанавливает HTTP-статус ответа.
     *
     * @param status HTTP-статус
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Возвращает длительность обработки запроса.
     *
     * @return длительность в миллисекундах
     */
    public long getDurationMs() {
        return durationMs;
    }

    /**
     * Устанавливает длительность обработки запроса.
     *
     * @param durationMs длительность в миллисекундах
     */
    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    /**
     * Возвращает IP-адрес клиента.
     *
     * @return IP-адрес
     */
    public String getRemoteAddr() {
        return remoteAddr;
    }

    /**
     * Устанавливает IP-адрес клиента.
     *
     * @param remoteAddr IP-адрес
     */
    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    /**
     * Возвращает заголовок User-Agent.
     *
     * @return User-Agent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Устанавливает заголовок User-Agent.
     *
     * @param userAgent User-Agent
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Возвращает дату и время создания записи.
     *
     * @return дата и время создания
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Устанавливает дату и время создания записи.
     *
     * @param createdAt дата и время создания
     */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
