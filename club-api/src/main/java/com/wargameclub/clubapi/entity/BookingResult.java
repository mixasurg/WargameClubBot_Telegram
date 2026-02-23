package com.wargameclub.clubapi.entity;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.enums.GameOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * JPA-сущность результат игры.
 */
@Entity
@Table(name = "booking_result")
public class BookingResult {

    /**
     * Поле состояния.
     */
    @Id
    @Column(name = "booking_id")
    private Long bookingId;

    /**
     * Поле состояния.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    /**
     * Поле состояния.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporter;

    /**
     * Поле состояния.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GameOutcome outcome;

    /**
     * Поле состояния.
     */
    @Column(nullable = false)
    private OffsetDateTime recordedAt = OffsetDateTime.now();

    /**
     * Конструктор BookingResult.
     */
    public BookingResult() {
    }

    /**
     * Конструктор BookingResult.
     */
    public BookingResult(Booking booking, User reporter, GameOutcome outcome) {
        this.booking = booking;
        this.bookingId = booking != null ? booking.getId() : null;
        this.reporter = reporter;
        this.outcome = outcome;
        this.recordedAt = OffsetDateTime.now();
    }

    /**
     * Возвращает идентификатор бронирования.
     */
    public Long getBookingId() {
        return bookingId;
    }

    /**
     * Возвращает бронирование.
     */
    public Booking getBooking() {
        return booking;
    }

    /**
     * Возвращает Reporter.
     */
    public User getReporter() {
        return reporter;
    }

    /**
     * Возвращает Outcome.
     */
    public GameOutcome getOutcome() {
        return outcome;
    }

    /**
     * Возвращает RecordedAt.
     */
    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }
}
