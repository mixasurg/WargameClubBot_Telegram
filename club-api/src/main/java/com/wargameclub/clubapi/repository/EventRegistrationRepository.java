package com.wargameclub.clubapi.repository;

import java.util.Optional;
import com.wargameclub.clubapi.entity.EventRegistration;
import com.wargameclub.clubapi.enums.RegistrationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

/**
 * JPA-репозиторий для регистраций на мероприятия.
 */
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    /**
     * Возвращает регистрацию пользователя на мероприятие.
     *
     * @param eventId идентификатор мероприятия
     * @param userId идентификатор пользователя
     * @return регистрация, если найдена
     */
    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);

    /**
     * Возвращает и блокирует регистрацию пользователя на мероприятие.
     *
     * @param eventId идентификатор мероприятия
     * @param userId идентификатор пользователя
     * @return регистрация, если найдена
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select er from EventRegistration er " +
            "join fetch er.event e " +
            "join fetch er.user u " +
            "where e.id = :eventId and u.id = :userId")
    Optional<EventRegistration> findByEventIdAndUserIdForUpdate(
            @Param("eventId") Long eventId,
            @Param("userId") Long userId
    );

    /**
     * Подсчитывает количество регистраций в указанном статусе.
     *
     * @param eventId идентификатор мероприятия
     * @param status статус регистрации
     * @return количество регистраций
     */
    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    /**
     * Подсчитывает количество регистраций в списке статусов.
     *
     * @param eventId идентификатор мероприятия
     * @param statuses список статусов регистрации
     * @return количество регистраций
     */
    long countByEventIdAndStatusIn(Long eventId, java.util.Collection<RegistrationStatus> statuses);
}
