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
 * JPA-сущность регистрации пользователя на мероприятие.
 */
@Entity
@Table(name = "event_registration")
public class EventRegistration {

    /**
     * Идентификатор регистрации.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Мероприятие, на которое выполнена регистрация.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private ClubEvent event;

    /**
     * Пользователь, зарегистрированный на мероприятие.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Статус регистрации.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    /**
     * Дата и время создания записи.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Создает пустую сущность для JPA.
     */
    public EventRegistration() {
    }

    /**
     * Создает регистрацию пользователя на мероприятие.
     *
     * @param event мероприятие
     * @param user пользователь
     */
    public EventRegistration(ClubEvent event, User user) {
        this.event = event;
        this.user = user;
        this.status = RegistrationStatus.REGISTERED;
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * Возвращает идентификатор регистрации.
     *
     * @return идентификатор регистрации
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает мероприятие регистрации.
     *
     * @return мероприятие
     */
    public ClubEvent getEvent() {
        return event;
    }

    /**
     * Устанавливает мероприятие регистрации.
     *
     * @param event мероприятие
     */
    public void setEvent(ClubEvent event) {
        this.event = event;
    }

    /**
     * Возвращает пользователя регистрации.
     *
     * @return пользователь
     */
    public User getUser() {
        return user;
    }

    /**
     * Устанавливает пользователя регистрации.
     *
     * @param user пользователь
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Возвращает статус регистрации.
     *
     * @return статус регистрации
     */
    public RegistrationStatus getStatus() {
        return status;
    }

    /**
     * Устанавливает статус регистрации.
     *
     * @param status статус регистрации
     */
    public void setStatus(RegistrationStatus status) {
        this.status = status;
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
