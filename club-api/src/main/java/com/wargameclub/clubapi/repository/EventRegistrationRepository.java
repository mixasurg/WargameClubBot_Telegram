package com.wargameclub.clubapi.repository;

import java.util.Optional;
import com.wargameclub.clubapi.entity.EventRegistration;
import com.wargameclub.clubapi.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для EventRegistration.
 */
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    /**
     * Возвращает EventRegistration.
     */
    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);

    /**
     * Выполняет операцию.
     */
    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);
}

