package com.wargameclub.clubapi.repository;

import java.util.Optional;
import com.wargameclub.clubapi.entity.EventRegistration;
import com.wargameclub.clubapi.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);

    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);
}

