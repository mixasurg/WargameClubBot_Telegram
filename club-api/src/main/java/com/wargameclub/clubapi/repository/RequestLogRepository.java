package com.wargameclub.clubapi.repository;

import com.wargameclub.clubapi.entity.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для логов HTTP-запросов.
 */
public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {
}
