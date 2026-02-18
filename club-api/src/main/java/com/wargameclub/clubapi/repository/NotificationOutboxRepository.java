package com.wargameclub.clubapi.repository;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.wargameclub.clubapi.entity.NotificationOutbox;
import com.wargameclub.clubapi.enums.NotificationStatus;
import com.wargameclub.clubapi.enums.NotificationTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, UUID> {
    Page<NotificationOutbox> findByTargetAndStatusAndNextAttemptAtLessThanEqual(
            NotificationTarget target,
            NotificationStatus status,
            OffsetDateTime now,
            Pageable pageable
    );

    void deleteByReferenceTypeAndReferenceIdAndStatus(
            String referenceType,
            Long referenceId,
            NotificationStatus status
    );
}

