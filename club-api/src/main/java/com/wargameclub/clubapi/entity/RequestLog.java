package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "request_log")
public class RequestLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String method;

    @Column(nullable = false, length = 300)
    private String path;

    @Column(length = 500)
    private String query;

    @Column(nullable = false)
    private int status;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "remote_addr", length = 100)
    private String remoteAddr;

    @Column(name = "user_agent", length = 300)
    private String userAgent;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public RequestLog() {
    }

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

    public Long getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
