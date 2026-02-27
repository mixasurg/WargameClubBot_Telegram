package com.wargameclub.clubapi.repository;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.wargameclub.clubapi.entity.NotificationOutbox;
import com.wargameclub.clubapi.enums.NotificationStatus;
import com.wargameclub.clubapi.enums.NotificationTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для outbox-очереди уведомлений.
 */
public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, UUID> {

    /**
     * Возвращает ожидающие отправки уведомления до указанного времени.
     *
     * @param target канал уведомления
     * @param status статус уведомления
     * @param now время, не позже которого должна быть попытка
     * @param pageable параметры пагинации
     * @return страница уведомлений
     */
    Page<NotificationOutbox> findByTargetAndStatusAndNextAttemptAtLessThanEqual(
            NotificationTarget target,
            NotificationStatus status,
            OffsetDateTime now,
            Pageable pageable
    );

    /**
     * Удаляет ожидающие уведомления по ссылочному типу и идентификатору.
     *
     * @param referenceType тип связанной сущности
     * @param referenceId идентификатор связанной сущности
     * @param status статус уведомления
     */
    void deleteByReferenceTypeAndReferenceIdAndStatus(
            String referenceType,
            Long referenceId,
            NotificationStatus status
    );
}
