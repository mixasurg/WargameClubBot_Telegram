package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import com.wargameclub.clubapi.entity.AuditLog;
import com.wargameclub.clubapi.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис записи аудита действий пользователей.
 */
@Service
public class AuditLogService {

    /**
     * Репозиторий записей аудита.
     */
    private final AuditLogRepository auditLogRepository;

    /**
     * Создает сервис аудита.
     *
     * @param auditLogRepository репозиторий записей аудита
     */
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Сохраняет запись аудита.
     *
     * @param actorUserId идентификатор пользователя
     * @param actorLogin логин пользователя
     * @param actorRole роль пользователя
     * @param action действие
     * @param method HTTP-метод
     * @param path путь
     * @param query query-строка
     * @param status HTTP-статус
     * @param clientIp IP клиента
     * @param userAgent User-Agent
     * @param details дополнительные детали
     */
    @Transactional
    public void log(
            Long actorUserId,
            String actorLogin,
            String actorRole,
            String action,
            String method,
            String path,
            String query,
            Integer status,
            String clientIp,
            String userAgent,
            String details
    ) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActorUserId(actorUserId);
        auditLog.setActorLogin(actorLogin);
        auditLog.setActorRole(actorRole);
        auditLog.setAction(action);
        auditLog.setHttpMethod(method);
        auditLog.setPath(path);
        auditLog.setQuery(query);
        auditLog.setStatus(status);
        auditLog.setClientIp(clientIp);
        auditLog.setUserAgent(userAgent);
        auditLog.setDetails(details);
        auditLog.setOccurredAt(OffsetDateTime.now());
        auditLogRepository.save(auditLog);
    }
}
