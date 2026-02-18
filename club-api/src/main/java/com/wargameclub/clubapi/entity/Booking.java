package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.BookingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private ClubTable table;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 120)
    private String game;

    @Column(nullable = false)
    private OffsetDateTime startAt;

    @Column(nullable = false)
    private OffsetDateTime endAt;

    @Column(name = "table_units", nullable = false)
    private int tableUnits = 2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent_user_id")
    private User opponent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "army_id")
    private Army army;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "table_assignments", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String tableAssignments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.CREATED;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Booking() {
    }

    public Booking(ClubTable table, User user, OffsetDateTime startAt, OffsetDateTime endAt) {
        this.table = table;
        this.user = user;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = BookingStatus.CREATED;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ClubTable getTable() {
        return table;
    }

    public void setTable(ClubTable table) {
        this.table = table;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public int getTableUnits() {
        return tableUnits;
    }

    public void setTableUnits(int tableUnits) {
        this.tableUnits = tableUnits;
    }

    public User getOpponent() {
        return opponent;
    }

    public void setOpponent(User opponent) {
        this.opponent = opponent;
    }

    public Army getArmy() {
        return army;
    }

    public void setArmy(Army army) {
        this.army = army;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTableAssignments() {
        return tableAssignments;
    }

    public void setTableAssignments(String tableAssignments) {
        this.tableAssignments = tableAssignments;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

