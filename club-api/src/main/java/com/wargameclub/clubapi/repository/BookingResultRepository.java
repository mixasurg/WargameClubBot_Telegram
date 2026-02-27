package com.wargameclub.clubapi.repository;

import com.wargameclub.clubapi.entity.BookingResult;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для результатов игр по бронированиям.
 */
public interface BookingResultRepository extends JpaRepository<BookingResult, Long> {
}
