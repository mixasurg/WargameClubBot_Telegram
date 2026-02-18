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

@Entity
@Table(name = "notification_outbox")
public class NotificationOutbox {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationTarget target;

    @Column(nullable = false, columnDefinition = "text")
    private String chatRouting;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(nullable = false)
    private OffsetDateTime nextAttemptAt = OffsetDateTime.now();

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column
    private OffsetDateTime sentAt;

    @Column(columnDefinition = "text")
    private String lastError;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    public NotificationOutbox() {
    }

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public NotificationTarget getTarget() {
        return target;
    }

    public void setTarget(NotificationTarget target) {
        this.target = target;
    }

    public String getChatRouting() {
        return chatRouting;
    }

    public void setChatRouting(String chatRouting) {
        this.chatRouting = chatRouting;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public OffsetDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public void setNextAttemptAt(OffsetDateTime nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(OffsetDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }
}

