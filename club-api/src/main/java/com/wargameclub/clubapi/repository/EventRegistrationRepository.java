package com.wargameclub.clubapi.repository;

import java.util.Optional;
import com.wargameclub.clubapi.entity.EventRegistration;
import com.wargameclub.clubapi.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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
     * Подсчитывает количество регистраций в указанном статусе.
     *
     * @param eventId идентификатор мероприятия
     * @param status статус регистрации
     * @return количество регистраций
     */
    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);
}
