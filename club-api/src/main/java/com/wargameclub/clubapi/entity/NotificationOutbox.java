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
 * JPA-сущность NotificationOutbox.
 */
@Entity
@Table(name = "notification_outbox")
public class NotificationOutbox {

    /**
     * Поле состояния.
     */
    @Id
    private UUID id;

    /**
     * Поле состояния.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationTarget target;

    /**
     * Поле состояния.
     */
    @Column(nullable = false, columnDefinition = "text")
    private String chatRouting;

    /**
     * Поле состояния.
     */
    @Column(nullable = false, columnDefinition = "text")
    private String text;

    /**
     * Поле состояния.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private int attempts = 0;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private OffsetDateTime nextAttemptAt = OffsetDateTime.now();

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /**
     * Поле состояния.
     */
    @Column
    private OffsetDateTime sentAt;

    /**
     * Поле состояния.
     */
    @Column(columnDefinition = "text")
    private String lastError;

    /**
     * Поле состояния.
     */
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * Поле состояния.
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * Конструктор NotificationOutbox.
     */
    public NotificationOutbox() {
    }

    /**
     * Конструктор NotificationOutbox.
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
     * Возвращает идентификатор.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Возвращает Target.
     */
    public NotificationTarget getTarget() {
        return target;
    }

    /**
     * Устанавливает Target.
     */
    public void setTarget(NotificationTarget target) {
        this.target = target;
    }

    /**
     * Возвращает ChatRouting.
     */
    public String getChatRouting() {
        return chatRouting;
    }

    /**
     * Устанавливает ChatRouting.
     */
    public void setChatRouting(String chatRouting) {
        this.chatRouting = chatRouting;
    }

    /**
     * Возвращает Text.
     */
    public String getText() {
        return text;
    }

    /**
     * Устанавливает Text.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Возвращает Status.
     */
    public NotificationStatus getStatus() {
        return status;
    }

    /**
     * Устанавливает Status.
     */
    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    /**
     * Возвращает Attempts.
     */
    public int getAttempts() {
        return attempts;
    }

    /**
     * Устанавливает Attempts.
     */
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    /**
     * Возвращает NextAttemptAt.
     */
    public OffsetDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    /**
     * Устанавливает NextAttemptAt.
     */
    public void setNextAttemptAt(OffsetDateTime nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
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
     * Возвращает SentAt.
     */
    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    /**
     * Устанавливает SentAt.
     */
    public void setSentAt(OffsetDateTime sentAt) {
        this.sentAt = sentAt;
    }

    /**
     * Возвращает LastError.
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Устанавливает LastError.
     */
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /**
     * Возвращает ReferenceType.
     */
    public String getReferenceType() {
        return referenceType;
    }

    /**
     * Устанавливает ReferenceType.
     */
    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    /**
     * Возвращает идентификатор Reference.
     */
    public Long getReferenceId() {
        return referenceId;
    }

    /**
     * Устанавливает идентификатор Reference.
     */
    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }
}

