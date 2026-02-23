package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.RegistrationStatus;
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
 * JPA-сущность EventRegistration.
 */
@Entity
@Table(name = "event_registration")
public class EventRegistration {

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
    @JoinColumn(name = "event_id", nullable = false)
    private ClubEvent event;

    /**
     * Поле состояния.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Поле состояния.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Конструктор EventRegistration.
     */
    public EventRegistration() {
    }

    /**
     * Конструктор EventRegistration.
     */
    public EventRegistration(ClubEvent event, User user) {
        this.event = event;
        this.user = user;
        this.status = RegistrationStatus.REGISTERED;
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * Возвращает идентификатор.
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает мероприятие.
     */
    public ClubEvent getEvent() {
        return event;
    }

    /**
     * Устанавливает мероприятие.
     */
    public void setEvent(ClubEvent event) {
        this.event = event;
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
     * Возвращает Status.
     */
    public RegistrationStatus getStatus() {
        return status;
    }

    /**
     * Устанавливает Status.
     */
    public void setStatus(RegistrationStatus status) {
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

