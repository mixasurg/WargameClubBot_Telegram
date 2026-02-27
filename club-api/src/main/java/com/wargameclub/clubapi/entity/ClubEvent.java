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
 * JPA-сущность мероприятия клуба.
 */
@Entity
@Table(name = "club_event")
public class ClubEvent {

    /**
     * Идентификатор мероприятия.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название мероприятия.
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Тип мероприятия.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventType type;

    /**
     * Описание мероприятия.
     */
    @Column
    private String description;

    /**
     * Дата и время начала.
     */
    @Column(nullable = false)
    private OffsetDateTime startAt;

    /**
     * Дата и время окончания.
     */
    @Column(nullable = false)
    private OffsetDateTime endAt;

    /**
     * Организатор мероприятия.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_user_id", nullable = false)
    private User organizer;

    /**
     * Максимальное число участников (опционально).
     */
    @Column
    private Integer capacity;

    /**
     * Текущий статус мероприятия.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.SCHEDULED;

    /**
     * Дата и время создания записи.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Дата и время последнего обновления.
     */
    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    /**
     * Создает пустую сущность для JPA.
     */
    public ClubEvent() {
    }

    /**
     * Возвращает идентификатор мероприятия.
     *
     * @return идентификатор мероприятия
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает название мероприятия.
     *
     * @return название мероприятия
     */
    public String getTitle() {
        return title;
    }

    /**
     * Устанавливает название мероприятия.
     *
     * @param title название мероприятия
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Возвращает тип мероприятия.
     *
     * @return тип мероприятия
     */
    public EventType getType() {
        return type;
    }

    /**
     * Устанавливает тип мероприятия.
     *
     * @param type тип мероприятия
     */
    public void setType(EventType type) {
        this.type = type;
    }

    /**
     * Возвращает описание мероприятия.
     *
     * @return описание мероприятия
     */
    public String getDescription() {
        return description;
    }

    /**
     * Устанавливает описание мероприятия.
     *
     * @param description описание мероприятия
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Возвращает дату и время начала.
     *
     * @return дата и время начала
     */
    public OffsetDateTime getStartAt() {
        return startAt;
    }

    /**
     * Устанавливает дату и время начала.
     *
     * @param startAt дата и время начала
     */
    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    /**
     * Возвращает дату и время окончания.
     *
     * @return дата и время окончания
     */
    public OffsetDateTime getEndAt() {
        return endAt;
    }

    /**
     * Устанавливает дату и время окончания.
     *
     * @param endAt дата и время окончания
     */
    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    /**
     * Возвращает организатора мероприятия.
     *
     * @return организатор мероприятия
     */
    public User getOrganizer() {
        return organizer;
    }

    /**
     * Устанавливает организатора мероприятия.
     *
     * @param organizer организатор мероприятия
     */
    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    /**
     * Возвращает лимит по числу участников.
     *
     * @return лимит участников или null
     */
    public Integer getCapacity() {
        return capacity;
    }

    /**
     * Устанавливает лимит по числу участников.
     *
     * @param capacity лимит участников
     */
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    /**
     * Возвращает текущий статус мероприятия.
     *
     * @return статус мероприятия
     */
    public EventStatus getStatus() {
        return status;
    }

    /**
     * Устанавливает текущий статус мероприятия.
     *
     * @param status статус мероприятия
     */
    public void setStatus(EventStatus status) {
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

    /**
     * Возвращает дату и время последнего обновления.
     *
     * @return дата и время обновления
     */
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Устанавливает дату и время последнего обновления.
     *
     * @param updatedAt дата и время обновления
     */
    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
