package com.wargameclub.clubapi.service;

import com.wargameclub.clubapi.entity.RequestLog;
import com.wargameclub.clubapi.repository.RequestLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для работы с сущностью RequestLog.
 */
@Service
public class RequestLogService {
    /**
     * Логгер.
     */
    private static final Logger log = LoggerFactory.getLogger(RequestLogService.class);

    /**
     * Поле состояния.
     */
    private static final int METHOD_MAX = 10;

    /**
     * Поле состояния.
     */
    private static final int PATH_MAX = 300;

    /**
     * Поле состояния.
     */
    private static final int QUERY_MAX = 500;

    /**
     * Поле состояния.
     */
    private static final int REMOTE_ADDR_MAX = 100;

    /**
     * Поле состояния.
     */
    private static final int USER_AGENT_MAX = 300;

    /**
     * Репозиторий RequestLog.
     */
    private final RequestLogRepository repository;

    /**
     * Конструктор RequestLogService.
     */
    public RequestLogService(RequestLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Выполняет операцию.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRequest(
            String method,
            String path,
            String query,
            int status,
            long durationMs,
            String remoteAddr,
            String userAgent
    ) {
        try {

            /**
             * Выполняет операцию.
             */
            RequestLog entry = new RequestLog(
                    truncate(method, METHOD_MAX),
                    truncate(path, PATH_MAX),
                    truncate(query, QUERY_MAX),
                    status,
                    durationMs,
                    truncate(remoteAddr, REMOTE_ADDR_MAX),
                    truncate(userAgent, USER_AGENT_MAX)
            );
            repository.save(entry);
        } catch (Exception ex) {
            log.warn("Не удалось сохранить лог запроса", ex);
        }
    }

    /**
     * Выполняет операцию.
     */
    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
