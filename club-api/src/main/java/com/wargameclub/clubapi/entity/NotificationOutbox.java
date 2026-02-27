package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.wargameclub.clubapi.enums.NotificationStatus;
import com.wargameclub.clubapi.enums.NotificationTarget;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA-сущность записи в outbox-очереди уведомлений.
 */
@Entity
@Table(name = "notification_outbox")
public class NotificationOutbox {

    /**
     * Идентификатор уведомления.
     */
    @Id
    private UUID id;

    /**
     * Целевой канал доставки уведомления.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationTarget target;

    /**
     * Маршрут/идентификатор получателя (например, chatId).
     */
    @Column(nullable = false, columnDefinition = "text")
    private String chatRouting;

    /**
     * Текст уведомления.
     */
    @Column(nullable = false, columnDefinition = "text")
    private String text;

    /**
     * Текущий статус отправки.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    /**
     * Количество попыток отправки.
     */
    @Column(nullable = false)
    private int attempts = 0;

    /**
     * Дата и время следующей попытки отправки.
     */
    @Column(nullable = false)
    private OffsetDateTime nextAttemptAt = OffsetDateTime.now();

    /**
     * Дата и время создания записи.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Дата и время успешной отправки (если отправлено).
     */
    @Column
    private OffsetDateTime sentAt;

    /**
     * Последняя ошибка отправки (если была).
     */
    @Column(columnDefinition = "text")
    private String lastError;

    /**
     * Тип связанной сущности (опционально).
     */
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * Идентификатор связанной сущности (опционально).
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * Создает пустую сущность для JPA.
     */
    public NotificationOutbox() {
    }

    /**
     * Создает запись outbox для отправки уведомления.
     *
     * @param id идентификатор уведомления
     * @param target целевой канал доставки
     * @param chatRouting маршрут/идентификатор получателя
     * @param text текст уведомления
     */
    public NotificationOutbox(UUID id, NotificationTarget target, String chatRouting, String text) {
        this.id = id;
        this.target = target;
        this.chatRouting = chatRouting;
        this.text = text;
        this.status = NotificationStatus.PENDING;
        this.attempts = 0;
        this.nextAttemptAt = OffsetDateTime.now();
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * Возвращает идентификатор уведомления.
     *
     * @return идентификатор уведомления
     */
    public UUID getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор уведомления.
     *
     * @param id идентификатор уведомления
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Возвращает целевой канал доставки.
     *
     * @return целевой канал доставки
     */
    public NotificationTarget getTarget() {
        return target;
    }

    /**
     * Устанавливает целевой канал доставки.
     *
     * @param target целевой канал доставки
     */
    public void setTarget(NotificationTarget target) {
        this.target = target;
    }

    /**
     * Возвращает маршрут/идентификатор получателя.
     *
     * @return маршрут получателя
     */
    public String getChatRouting() {
        return chatRouting;
    }

    /**
     * Устанавливает маршрут/идентификатор получателя.
     *
     * @param chatRouting маршрут получателя
     */
    public void setChatRouting(String chatRouting) {
        this.chatRouting = chatRouting;
    }

    /**
     * Возвращает текст уведомления.
     *
     * @return текст уведомления
     */
    public String getText() {
        return text;
    }

    /**
     * Устанавливает текст уведомления.
     *
     * @param text текст уведомления
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Возвращает статус отправки.
     *
     * @return статус отправки
     */
    public NotificationStatus getStatus() {
        return status;
    }

    /**
     * Устанавливает статус отправки.
     *
     * @param status статус отправки
     */
    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    /**
     * Возвращает число попыток отправки.
     *
     * @return число попыток
     */
    public int getAttempts() {
        return attempts;
    }

    /**
     * Устанавливает число попыток отправки.
     *
     * @param attempts число попыток
     */
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    /**
     * Возвращает дату следующей попытки отправки.
     *
     * @return дата следующей попытки
     */
    public OffsetDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    /**
     * Устанавливает дату следующей попытки отправки.
     *
     * @param nextAttemptAt дата следующей попытки
     */
    public void setNextAttemptAt(OffsetDateTime nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
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
     * Возвращает дату и время успешной отправки.
     *
     * @return дата отправки или null
     */
    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    /**
     * Устанавливает дату и время успешной отправки.
     *
     * @param sentAt дата отправки
     */
    public void setSentAt(OffsetDateTime sentAt) {
        this.sentAt = sentAt;
    }

    /**
     * Возвращает последнюю ошибку отправки.
     *
     * @return последняя ошибка
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Устанавливает последнюю ошибку отправки.
     *
     * @param lastError последняя ошибка
     */
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /**
     * Возвращает тип связанной сущности.
     *
     * @return тип связанной сущности
     */
    public String getReferenceType() {
        return referenceType;
    }

    /**
     * Устанавливает тип связанной сущности.
     *
     * @param referenceType тип связанной сущности
     */
    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    /**
     * Возвращает идентификатор связанной сущности.
     *
     * @return идентификатор связанной сущности
     */
    public Long getReferenceId() {
        return referenceId;
    }

    /**
     * Устанавливает идентификатор связанной сущности.
     *
     * @param referenceId идентификатор связанной сущности
     */
    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }
}
