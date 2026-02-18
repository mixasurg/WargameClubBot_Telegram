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

@Entity
@Table(name = "army_usage")
public class ArmyUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "army_id", nullable = false)
    private Army army;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_by_user_id", nullable = false)
    private User usedBy;

    @Column(nullable = false)
    private OffsetDateTime usedAt;

    @Column
    private String notes;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public ArmyUsage() {
    }

    public ArmyUsage(Army army, User usedBy, OffsetDateTime usedAt, String notes) {
        this.army = army;
        this.usedBy = usedBy;
        this.usedAt = usedAt;
        this.notes = notes;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Army getArmy() {
        return army;
    }

    public void setArmy(Army army) {
        this.army = army;
    }

    public User getUsedBy() {
        return usedBy;
    }

    public void setUsedBy(User usedBy) {
        this.usedBy = usedBy;
    }

    public OffsetDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(OffsetDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

