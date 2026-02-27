package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wargameclub.clubapi.config.AppProperties;
import com.wargameclub.clubapi.dto.NotificationOutboxDto;
import com.wargameclub.clubapi.entity.NotificationOutbox;
import com.wargameclub.clubapi.enums.NotificationStatus;
import com.wargameclub.clubapi.enums.NotificationTarget;
import com.wargameclub.clubapi.exception.BadRequestException;
import com.wargameclub.clubapi.exception.NotFoundException;
import com.wargameclub.clubapi.repository.NotificationOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис работы с outbox-очередью уведомлений.
 */
@Service
public class NotificationOutboxService {
    /**
     * Логгер.
     */
    private static final Logger log = LoggerFactory.getLogger(NotificationOutboxService.class);

    /**
     * Репозиторий outbox-уведомлений.
     */
    private final NotificationOutboxRepository repository;

    /**
     * Сериализатор JSON.
     */
    private final ObjectMapper objectMapper;

    /**
     * Настройки приложения.
     */
    private final AppProperties appProperties;

    /**
     * Создает сервис outbox-уведомлений.
     *
     * @param repository репозиторий outbox-уведомлений
     * @param objectMapper сериализатор JSON
     * @param appProperties настройки приложения
     */
    public NotificationOutboxService(
            NotificationOutboxRepository repository,
            ObjectMapper objectMapper,
            AppProperties appProperties
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
    }

    /**
     * Ставит уведомление в очередь на отправку немедленно.
     *
     * @param target канал уведомления
     * @param routing маршрут доставки
     * @param text текст уведомления
     * @return созданная запись outbox
     */
    @Transactional
    public NotificationOutbox enqueue(NotificationTarget target, ChatRouting routing, String text) {
        return enqueueAt(target, routing, text, null, null, null);
    }

    /**
     * Ставит уведомление в очередь на отправку в указанное время.
     *
     * @param target канал уведомления
     * @param routing маршрут доставки
     * @param text текст уведомления
     * @param nextAttemptAt время первой попытки отправки
     * @param referenceType тип связанной сущности (опционально)
     * @param referenceId идентификатор связанной сущности (опционально)
     * @return созданная запись outbox
     */
    @Transactional
    public NotificationOutbox enqueueAt(
            NotificationTarget target,
            ChatRouting routing,
            String text,
            OffsetDateTime nextAttemptAt,
            String referenceType,
            Long referenceId
    ) {
        if (routing == null || routing.chatId() == null) {
            throw new BadRequestException("Не задан маршрут уведомления");
        }
        String routingJson;
        try {
            routingJson = objectMapper.writeValueAsString(routing);
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Не удалось сериализовать маршрут уведомления");
        }
        NotificationOutbox outbox = new NotificationOutbox(UUID.randomUUID(), target, routingJson, text);
        if (nextAttemptAt != null) {
            outbox.setNextAttemptAt(nextAttemptAt);
        }
        outbox.setReferenceType(referenceType);
        outbox.setReferenceId(referenceId);
        return repository.save(outbox);
    }

    /**
     * Возвращает список ожидающих отправки уведомлений.
     *
     * @param target канал уведомления
     * @param limit максимальное число записей
     * @return список DTO уведомлений
     */
    @Transactional(readOnly = true)
    public List<NotificationOutboxDto> getPending(NotificationTarget target, int limit) {
        return repository.findByTargetAndStatusAndNextAttemptAtLessThanEqual(
                        target,
                        NotificationStatus.PENDING,
                        OffsetDateTime.now(),
                        PageRequest.of(0, limit)
                ).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Отмечает уведомление как успешно отправленное.
     *
     * @param id идентификатор уведомления
     */
    @Transactional
    public void markSent(UUID id) {
        NotificationOutbox outbox = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Уведомление не найдено: " + id));
        outbox.setStatus(NotificationStatus.SENT);
        outbox.setSentAt(OffsetDateTime.now());
        outbox.setLastError(null);
    }

    /**
     * Отмечает уведомление как ошибочное и планирует повторную попытку.
     *
     * @param id идентификатор уведомления
     * @param error сообщение об ошибке
     */
    @Transactional
    public void markFailed(UUID id, String error) {
        NotificationOutbox outbox = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Уведомление не найдено: " + id));
        int attempts = outbox.getAttempts() + 1;
        outbox.setAttempts(attempts);
        outbox.setLastError(error);
        int maxAttempts = appProperties.getNotifications().getMaxAttempts();
        if (attempts >= maxAttempts) {
            outbox.setStatus(NotificationStatus.FAILED);
            log.warn("Уведомление {} переведено в FAILED после {} попыток", id, attempts);
            return;
        }
        outbox.setStatus(NotificationStatus.PENDING);
        outbox.setNextAttemptAt(OffsetDateTime.now().plusSeconds(appProperties.getNotifications().getBackoffSeconds()));
    }

    /**
     * Удаляет ожидающие уведомления по ссылочному типу и идентификатору.
     *
     * @param referenceType тип связанной сущности
     * @param referenceId идентификатор связанной сущности
     */
    @Transactional
    public void deletePendingByReference(String referenceType, Long referenceId) {
        if (referenceType == null || referenceId == null) {
            return;
        }
        repository.deleteByReferenceTypeAndReferenceIdAndStatus(
                referenceType,
                referenceId,
                NotificationStatus.PENDING
        );
    }

    /**
     * Преобразует сущность outbox в DTO.
     *
     * @param outbox запись outbox
     * @return DTO уведомления
     */
    private NotificationOutboxDto toDto(NotificationOutbox outbox) {
        return new NotificationOutboxDto(
                outbox.getId(),
                outbox.getTarget(),
                outbox.getChatRouting(),
                outbox.getText(),
                outbox.getStatus(),
                outbox.getAttempts(),
                outbox.getNextAttemptAt(),
                outbox.getCreatedAt()
        );
    }
}
