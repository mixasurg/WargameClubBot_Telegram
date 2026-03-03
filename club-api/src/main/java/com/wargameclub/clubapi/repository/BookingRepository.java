package com.wargameclub.clubapi.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import com.wargameclub.clubapi.entity.Booking;
import com.wargameclub.clubapi.enums.BookingMode;
import com.wargameclub.clubapi.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA-репозиторий для бронирований.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Проверяет наличие пересекающегося бронирования для стола.
     *
     * @param tableId идентификатор стола
     * @param status статус бронирования
     * @param endAt конец интервала
     * @param startAt начало интервала
     * @return true, если пересечение найдено
     */
    boolean existsByTableIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
            Long tableId,
            BookingStatus status,
            OffsetDateTime endAt,
            OffsetDateTime startAt
    );

    /**
     * Возвращает бронирования с деталями (таблица, пользователь, соперник, армия), пересекающие интервал.
     *
     * @param status статус бронирования
     * @param from начало интервала
     * @param to конец интервала
     * @return список бронирований
     */
    @Query("select b from Booking b " +
            "left join fetch b.table t " +
            "join fetch b.user u " +
            "left join fetch b.opponent o " +
            "left join fetch b.army a " +
            "where b.status = :status and b.startAt < :to and b.endAt > :from")
    List<Booking> findOverlappingWithDetails(
            @Param("status") BookingStatus status,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    /**
     * Возвращает бронирования указанного стола с деталями, пересекающие интервал.
     *
     * @param status статус бронирования
     * @param tableId идентификатор стола
     * @param from начало интервала
     * @param to конец интервала
     * @return список бронирований
     */
    @Query("select b from Booking b " +
            "left join fetch b.table t " +
            "join fetch b.user u " +
            "left join fetch b.opponent o " +
            "left join fetch b.army a " +
            "where b.status = :status and b.table.id = :tableId and b.startAt < :to and b.endAt > :from")
    List<Booking> findOverlappingWithDetailsForTable(
            @Param("status") BookingStatus status,
            @Param("tableId") Long tableId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    /**
     * Возвращает все бронирования с деталями, пересекающие интервал (без фильтра по статусу).
     *
     * @param from начало интервала
     * @param to конец интервала
     * @return список бронирований
     */
    @Query("select b from Booking b " +
            "left join fetch b.table t " +
            "join fetch b.user u " +
            "left join fetch b.opponent o " +
            "left join fetch b.army a " +
            "where b.startAt < :to and b.endAt > :from")
    List<Booking> findOverlappingWithDetailsAll(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    /**
     * Возвращает бронирования указанного стола с деталями, пересекающие интервал (без фильтра по статусу).
     *
     * @param tableId идентификатор стола
     * @param from начало интервала
     * @param to конец интервала
     * @return список бронирований
     */
    @Query("select b from Booking b " +
            "left join fetch b.table t " +
            "join fetch b.user u " +
            "left join fetch b.opponent o " +
            "left join fetch b.army a " +
            "where b.table.id = :tableId and b.startAt < :to and b.endAt > :from")
    List<Booking> findOverlappingWithDetailsForTableAll(
            @Param("tableId") Long tableId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    /**
     * Возвращает открытые бронирования в заданном интервале.
     *
     * @param status статус бронирования
     * @param mode режим бронирования
     * @param from начало интервала
     * @param to конец интервала
     * @return список открытых бронирований
     */
    @Query("select b from Booking b " +
            "left join fetch b.table t " +
            "join fetch b.user u " +
            "left join fetch b.opponent o " +
            "left join fetch b.army a " +
            "where b.status = :status and b.bookingMode = :mode and b.opponent is null " +
            "and b.startAt < :to and b.endAt > :from")
    List<Booking> findOpenWithDetails(
            @Param("status") BookingStatus status,
            @Param("mode") BookingMode mode,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    /**
     * Возвращает открытые бронирования в заданном интервале с фильтром по игре.
     *
     * @param status статус бронирования
     * @param mode режим бронирования
     * @param from начало интервала
     * @param to конец интервала
     * @param game фильтр по игре
     * @return список открытых бронирований
     */
    @Query("select b from Booking b " +
            "left join fetch b.table t " +
            "join fetch b.user u " +
            "left join fetch b.opponent o " +
            "left join fetch b.army a " +
            "where b.status = :status and b.bookingMode = :mode and b.opponent is null " +
            "and b.startAt < :to and b.endAt > :from " +
            "and lower(b.game) like lower(concat('%', :game, '%'))")
    List<Booking> findOpenWithDetailsByGame(
            @Param("status") BookingStatus status,
            @Param("mode") BookingMode mode,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            @Param("game") String game
    );

    /**
     * Возвращает и блокирует бронирование по идентификатору.
     *
     * @param id идентификатор бронирования
     * @return бронирование
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b " +
            "left join fetch b.table t " +
            "join fetch b.user u " +
            "left join fetch b.opponent o " +
            "left join fetch b.army a " +
            "where b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") Long id);

    /**
     * Возвращает и блокирует открытые бронирования с истекшим дедлайном.
     *
     * @param status статус бронирования
     * @param mode режим бронирования
     * @param now текущий момент
     * @return список бронирований
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b " +
            "left join fetch b.user u " +
            "left join fetch b.opponent o " +
            "where b.status = :status and b.bookingMode = :mode and b.opponent is null " +
            "and b.joinDeadlineAt is not null and b.joinDeadlineAt <= :now")
    List<Booking> findExpiredOpenForUpdate(
            @Param("status") BookingStatus status,
            @Param("mode") BookingMode mode,
            @Param("now") OffsetDateTime now
    );
}
