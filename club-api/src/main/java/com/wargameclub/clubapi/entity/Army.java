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
@Table(name = "army")
public class Army {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String game;

    @Column(nullable = false, length = 100)
    private String faction;

    @Column(nullable = false)
    private boolean isClubShared = false;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Army() {
    }

    public Army(User owner, String game, String faction, boolean isClubShared) {
        this.owner = owner;
        this.game = game;
        this.faction = faction;
        this.isClubShared = isClubShared;
        this.isActive = true;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public boolean isClubShared() {
        return isClubShared;
    }

    public void setClubShared(boolean clubShared) {
        isClubShared = clubShared;
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

