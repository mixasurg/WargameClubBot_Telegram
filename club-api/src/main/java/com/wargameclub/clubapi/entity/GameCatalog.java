package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "game_catalog")
public class GameCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, unique = true)
    private String name;

    @Column(nullable = false)
    private int defaultDurationMinutes;

    @Column(nullable = false)
    private int tableUnits;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public GameCatalog() {
    }

    public GameCatalog(String name, int defaultDurationMinutes, int tableUnits) {
        this.name = name;
        this.defaultDurationMinutes = defaultDurationMinutes;
        this.tableUnits = tableUnits;
        this.isActive = true;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDefaultDurationMinutes() {
        return defaultDurationMinutes;
    }

    public void setDefaultDurationMinutes(int defaultDurationMinutes) {
        this.defaultDurationMinutes = defaultDurationMinutes;
    }

    public int getTableUnits() {
        return tableUnits;
    }

    public void setTableUnits(int tableUnits) {
        this.tableUnits = tableUnits;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

