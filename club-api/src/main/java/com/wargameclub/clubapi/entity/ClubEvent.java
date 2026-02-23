package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.EventStatus;
import com.wargameclub.clubapi.enums.EventType;
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

/**
 * JPA-сущность Club.
 */
@Entity
@Table(name = "club_event")
public class ClubEvent {

    /**
     * Поле состояния.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Поле состояния.
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Поле состояния.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventType type;

    /**
     * Поле состояния.
     */
    @Column
    private String description;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_user_id", nullable = false)
    private User organizer;

    /**
     * Поле состояния.
     */
    @Column
    private Integer capacity;

    /**
     * Поле состояния.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.SCHEDULED;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    /**
     * Конструктор ClubEvent.
     */
    public ClubEvent() {
    }

    /**
     * Возвращает идентификатор.
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает Title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Устанавливает Title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Возвращает Type.
     */
    public EventType getType() {
        return type;
    }

    /**
     * Устанавливает Type.
     */
    public void setType(EventType type) {
        this.type = type;
    }

    /**
     * Возвращает Description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Устанавливает Description.
     */
    public void setDescription(String description) {
        this.description = description;
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
     * Возвращает Organizer.
     */
    public User getOrganizer() {
        return organizer;
    }

    /**
     * Устанавливает Organizer.
     */
    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    /**
     * Возвращает Capacity.
     */
    public Integer getCapacity() {
        return capacity;
    }

    /**
     * Устанавливает Capacity.
     */
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    /**
     * Возвращает Status.
     */
    public EventStatus getStatus() {
        return status;
    }

    /**
     * Устанавливает Status.
     */
    public void setStatus(EventStatus status) {
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

    /**
     * Возвращает UpdatedAt.
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Устанавливает UpdatedAt.
     */
    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

