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
 * JPA-сущность результата игры, связанного с бронированием.
 */
@Entity
@Table(name = "booking_result")
public class BookingResult {

    /**
     * Идентификатор бронирования (также первичный ключ результата).
     */
    @Id
    @Column(name = "booking_id")
    private Long bookingId;

    /**
     * Связанное бронирование.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    /**
     * Пользователь, сообщивший результат.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporter;

    /**
     * Исход игры.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GameOutcome outcome;

    /**
     * Дата и время фиксации результата.
     */
    @Column(nullable = false)
    private OffsetDateTime recordedAt = OffsetDateTime.now();

    /**
     * Создает пустую сущность для JPA.
     */
    public BookingResult() {
    }

    /**
     * Создает результат игры для указанного бронирования.
     *
     * @param booking бронирование
     * @param reporter пользователь, сообщивший результат
     * @param outcome исход игры
     */
    public BookingResult(Booking booking, User reporter, GameOutcome outcome) {
        this.booking = booking;
        this.reporter = reporter;
        this.outcome = outcome;
        this.recordedAt = OffsetDateTime.now();
    }

    /**
     * Возвращает идентификатор бронирования.
     *
     * @return идентификатор бронирования
     */
    public Long getBookingId() {
        return bookingId;
    }

    /**
     * Возвращает связанное бронирование.
     *
     * @return бронирование
     */
    public Booking getBooking() {
        return booking;
    }

    /**
     * Возвращает пользователя, сообщившего результат.
     *
     * @return пользователь-репортер
     */
    public User getReporter() {
        return reporter;
    }

    /**
     * Возвращает исход игры.
     *
     * @return исход игры
     */
    public GameOutcome getOutcome() {
        return outcome;
    }

    /**
     * Возвращает дату и время фиксации результата.
     *
     * @return дата и время фиксации
     */
    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }
}
