package com.wargameclub.clubapi.repository;

import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubapi.entity.BookingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA-репозиторий для результатов игр по бронированиям.
 */
public interface BookingResultRepository extends JpaRepository<BookingResult, Long> {

    /**
     * Возвращает все результаты игр пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список результатов игр пользователя
     */
    @Query("select br from BookingResult br " +
            "join fetch br.booking b " +
            "join fetch br.reporter r " +
            "join fetch b.user u " +
            "left join fetch b.opponent o " +
            "where (u.id = :userId or o.id = :userId)")
    List<BookingResult> findByUserId(
            @Param("userId") Long userId
    );

    /**
     * Возвращает результаты игр пользователя начиная с указанной даты фиксации.
     *
     * @param userId идентификатор пользователя
     * @param from включительная нижняя граница по дате фиксации результата
     * @return список результатов игр пользователя
     */
    @Query("select br from BookingResult br " +
            "join fetch br.booking b " +
            "join fetch br.reporter r " +
            "join fetch b.user u " +
            "left join fetch b.opponent o " +
            "where (u.id = :userId or o.id = :userId) and br.recordedAt >= :from")
    List<BookingResult> findByUserIdAndRecordedAtFrom(
            @Param("userId") Long userId,
            @Param("from") OffsetDateTime from
    );
}
