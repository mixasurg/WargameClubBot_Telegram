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

@Entity
@Table(name = "booking_result")
public class BookingResult {
    @Id
    @Column(name = "booking_id")
    private Long bookingId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GameOutcome outcome;

    @Column(nullable = false)
    private OffsetDateTime recordedAt = OffsetDateTime.now();

    public BookingResult() {
    }

    public BookingResult(Booking booking, User reporter, GameOutcome outcome) {
        this.booking = booking;
        this.bookingId = booking != null ? booking.getId() : null;
        this.reporter = reporter;
        this.outcome = outcome;
        this.recordedAt = OffsetDateTime.now();
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Booking getBooking() {
        return booking;
    }

    public User getReporter() {
        return reporter;
    }

    public GameOutcome getOutcome() {
        return outcome;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }
}
