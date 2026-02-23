package com.wargameclub.clubapi.repository;

import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubapi.entity.Booking;
import com.wargameclub.clubapi.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA-репозиторий для бронирования.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Выполняет операцию.
     */
    boolean existsByTableIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
            Long tableId,
            BookingStatus status,
            OffsetDateTime endAt,
            OffsetDateTime startAt
    );

    @Query("select b from Booking b " +
            "left join fetch b.table t " +
            "join fetch b.user u " +
            "left join fetch b.opponent o " +
            "left join fetch b.army a " +
            "where b.status = :status and b.startAt < :to and b.endAt > :from")

    /**
     * Возвращает OverlappingWithDetails.
     */
    List<Booking> findOverlappingWithDetails(
            @Param("status") BookingStatus status,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("select b from Booking b " +
            "left join fetch b.table t " +
            "join fetch b.user u " +
            "left join fetch b.opponent o " +
            "left join fetch b.army a " +
            "where b.status = :status and b.table.id = :tableId and b.startAt < :to and b.endAt > :from")

    /**
     * Возвращает OverlappingWithDetailsForTable.
     */
    List<Booking> findOverlappingWithDetailsForTable(
            @Param("status") BookingStatus status,
            @Param("tableId") Long tableId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("select b from Booking b " +
            "left join fetch b.table t " +
            "join fetch b.user u " +
            "left join fetch b.opponent o " +
            "left join fetch b.army a " +
            "where b.startAt < :to and b.endAt > :from")

    /**
     * Возвращает OverlappingWithDetailsAll.
     */
    List<Booking> findOverlappingWithDetailsAll(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("select b from Booking b " +
            "left join fetch b.table t " +
            "join fetch b.user u " +
            "left join fetch b.opponent o " +
            "left join fetch b.army a " +
            "where b.table.id = :tableId and b.startAt < :to and b.endAt > :from")

    /**
     * Возвращает OverlappingWithDetailsForTableAll.
     */
    List<Booking> findOverlappingWithDetailsForTableAll(
            @Param("tableId") Long tableId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );
}

