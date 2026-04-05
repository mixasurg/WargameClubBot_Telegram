package com.wargameclub.clubapi.repository;

import com.wargameclub.clubapi.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий записей аудита.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
