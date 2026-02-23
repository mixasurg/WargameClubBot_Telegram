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

/**
 * JPA-сущность бронирование.
 */
@Entity
@Table(name = "booking")
public class Booking {

    /**
     * Поле состояния.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Поле состояния.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private ClubTable table;

    /**
     * Поле состояния.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Поле состояния.
     */
    @Column(length = 120)
    private String game;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private OffsetDateTime startAt;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private OffsetDateTime endAt;

    /**
     * Поле состояния.
     */
    @Column(name = "table_units", nullable = false)
    private int tableUnits = 2;

    /**
     * Поле состояния.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent_user_id")
    private User opponent;

    /**
     * Поле состояния.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "army_id")
    private Army army;

    /**
     * Поле состояния.
     */
    @Column(columnDefinition = "text")
    private String notes;

    /**
     * Поле состояния.
     */
    @Column(name = "table_assignments", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String tableAssignments;

    /**
     * Поле состояния.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.CREATED;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Конструктор Booking.
     */
    public Booking() {
    }

    /**
     * Конструктор Booking.
     */
    public Booking(ClubTable table, User user, OffsetDateTime startAt, OffsetDateTime endAt) {
        this.table = table;
        this.user = user;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = BookingStatus.CREATED;
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * Возвращает идентификатор.
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает стол.
     */
    public ClubTable getTable() {
        return table;
    }

    /**
     * Устанавливает стол.
     */
    public void setTable(ClubTable table) {
        this.table = table;
    }

    /**
     * Возвращает пользователя.
     */
    public User getUser() {
        return user;
    }

    /**
     * Устанавливает пользователя.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Возвращает время начала.
     */
    public OffsetDateTime getStartAt() {
        return startAt;
    }

    /**
     * Устанавливает время начала.
     */
    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    /**
     * Возвращает время окончания.
     */
    public OffsetDateTime getEndAt() {
        return endAt;
    }

    /**
     * Устанавливает время окончания.
     */
    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    /**
     * Возвращает игру.
     */
    public String getGame() {
        return game;
    }

    /**
     * Устанавливает игру.
     */
    public void setGame(String game) {
        this.game = game;
    }

    /**
     * Возвращает количество столов.
     */
    public int getTableUnits() {
        return tableUnits;
    }

    /**
     * Устанавливает количество столов.
     */
    public void setTableUnits(int tableUnits) {
        this.tableUnits = tableUnits;
    }

    /**
     * Возвращает соперника.
     */
    public User getOpponent() {
        return opponent;
    }

    /**
     * Устанавливает соперника.
     */
    public void setOpponent(User opponent) {
        this.opponent = opponent;
    }

    /**
     * Возвращает армию.
     */
    public Army getArmy() {
        return army;
    }

    /**
     * Устанавливает армию.
     */
    public void setArmy(Army army) {
        this.army = army;
    }

    /**
     * Возвращает Notes.
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Устанавливает Notes.
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Возвращает TableAssignments.
     */
    public String getTableAssignments() {
        return tableAssignments;
    }

    /**
     * Устанавливает TableAssignments.
     */
    public void setTableAssignments(String tableAssignments) {
        this.tableAssignments = tableAssignments;
    }

    /**
     * Возвращает Status.
     */
    public BookingStatus getStatus() {
        return status;
    }

    /**
     * Устанавливает Status.
     */
    public void setStatus(BookingStatus status) {
        this.status = status;
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

