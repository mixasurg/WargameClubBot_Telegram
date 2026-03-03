package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.BookingMode;
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
 * JPA-сущность бронирования стола для игры.
 */
@Entity
@Table(name = "booking")
public class Booking {

    /**
     * Идентификатор бронирования.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Основной стол бронирования.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private ClubTable table;

    /**
     * Пользователь, создавший бронирование.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Название игры/системы.
     */
    @Column(length = 120)
    private String game;

    /**
     * Дата и время начала бронирования.
     */
    @Column(nullable = false)
    private OffsetDateTime startAt;

    /**
     * Дата и время окончания бронирования.
     */
    @Column(nullable = false)
    private OffsetDateTime endAt;

    /**
     * Количество единиц стола, занятых бронированием.
     */
    @Column(name = "table_units", nullable = false)
    private int tableUnits = 2;

    /**
     * Соперник (опционально).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent_user_id")
    private User opponent;

    /**
     * Используемая армия (опционально).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "army_id")
    private Army army;

    /**
     * Дополнительные примечания.
     */
    @Column(columnDefinition = "text")
    private String notes;

    /**
     * JSON-распределение бронирования по столам.
     */
    @Column(name = "table_assignments", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String tableAssignments;

    /**
     * Статус бронирования.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.CREATED;

    /**
     * Режим бронирования.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_mode", nullable = false, length = 20)
    private BookingMode bookingMode = BookingMode.FIXED;

    /**
     * Срок, до которого можно присоединиться к открытой игре.
     */
    @Column(name = "join_deadline_at")
    private OffsetDateTime joinDeadlineAt;

    /**
     * Причина отмены бронирования.
     */
    @Column(name = "cancel_reason", length = 50)
    private String cancelReason;

    /**
     * Дата и время создания бронирования.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Создает пустую сущность для JPA.
     */
    public Booking() {
    }

    /**
     * Создает бронирование с основными параметрами.
     *
     * @param table забронированный стол
     * @param user пользователь, создавший бронирование
     * @param startAt дата и время начала
     * @param endAt дата и время окончания
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
     * Возвращает идентификатор бронирования.
     *
     * @return идентификатор бронирования
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает основной стол бронирования.
     *
     * @return стол бронирования
     */
    public ClubTable getTable() {
        return table;
    }

    /**
     * Устанавливает основной стол бронирования.
     *
     * @param table стол бронирования
     */
    public void setTable(ClubTable table) {
        this.table = table;
    }

    /**
     * Возвращает пользователя, создавшего бронирование.
     *
     * @return пользователь
     */
    public User getUser() {
        return user;
    }

    /**
     * Устанавливает пользователя, создавшего бронирование.
     *
     * @param user пользователь
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Возвращает дату и время начала бронирования.
     *
     * @return дата и время начала
     */
    public OffsetDateTime getStartAt() {
        return startAt;
    }

    /**
     * Устанавливает дату и время начала бронирования.
     *
     * @param startAt дата и время начала
     */
    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    /**
     * Возвращает дату и время окончания бронирования.
     *
     * @return дата и время окончания
     */
    public OffsetDateTime getEndAt() {
        return endAt;
    }

    /**
     * Устанавливает дату и время окончания бронирования.
     *
     * @param endAt дата и время окончания
     */
    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    /**
     * Возвращает название игры/системы.
     *
     * @return название игры
     */
    public String getGame() {
        return game;
    }

    /**
     * Устанавливает название игры/системы.
     *
     * @param game название игры
     */
    public void setGame(String game) {
        this.game = game;
    }

    /**
     * Возвращает количество единиц стола.
     *
     * @return количество единиц стола
     */
    public int getTableUnits() {
        return tableUnits;
    }

    /**
     * Устанавливает количество единиц стола.
     *
     * @param tableUnits количество единиц стола
     */
    public void setTableUnits(int tableUnits) {
        this.tableUnits = tableUnits;
    }

    /**
     * Возвращает соперника.
     *
     * @return соперник или null
     */
    public User getOpponent() {
        return opponent;
    }

    /**
     * Устанавливает соперника.
     *
     * @param opponent соперник
     */
    public void setOpponent(User opponent) {
        this.opponent = opponent;
    }

    /**
     * Возвращает выбранную армию.
     *
     * @return армия или null
     */
    public Army getArmy() {
        return army;
    }

    /**
     * Устанавливает выбранную армию.
     *
     * @param army армия
     */
    public void setArmy(Army army) {
        this.army = army;
    }

    /**
     * Возвращает примечания к бронированию.
     *
     * @return примечания
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Устанавливает примечания к бронированию.
     *
     * @param notes примечания
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Возвращает JSON-распределение по столам.
     *
     * @return JSON распределения по столам
     */
    public String getTableAssignments() {
        return tableAssignments;
    }

    /**
     * Устанавливает JSON-распределение по столам.
     *
     * @param tableAssignments JSON распределения по столам
     */
    public void setTableAssignments(String tableAssignments) {
        this.tableAssignments = tableAssignments;
    }

    /**
     * Возвращает статус бронирования.
     *
     * @return статус бронирования
     */
    public BookingStatus getStatus() {
        return status;
    }

    /**
     * Устанавливает статус бронирования.
     *
     * @param status статус бронирования
     */
    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    /**
     * Возвращает режим бронирования.
     *
     * @return режим бронирования
     */
    public BookingMode getBookingMode() {
        return bookingMode;
    }

    /**
     * Устанавливает режим бронирования.
     *
     * @param bookingMode режим бронирования
     */
    public void setBookingMode(BookingMode bookingMode) {
        this.bookingMode = bookingMode;
    }

    /**
     * Возвращает срок присоединения к открытой игре.
     *
     * @return срок присоединения или null
     */
    public OffsetDateTime getJoinDeadlineAt() {
        return joinDeadlineAt;
    }

    /**
     * Устанавливает срок присоединения к открытой игре.
     *
     * @param joinDeadlineAt срок присоединения
     */
    public void setJoinDeadlineAt(OffsetDateTime joinDeadlineAt) {
        this.joinDeadlineAt = joinDeadlineAt;
    }

    /**
     * Возвращает причину отмены бронирования.
     *
     * @return причина отмены или null
     */
    public String getCancelReason() {
        return cancelReason;
    }

    /**
     * Устанавливает причину отмены бронирования.
     *
     * @param cancelReason причина отмены
     */
    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    /**
     * Возвращает дату и время создания бронирования.
     *
     * @return дата и время создания
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Устанавливает дату и время создания бронирования.
     *
     * @param createdAt дата и время создания
     */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
