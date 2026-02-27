package com.wargameclub.clubapi.service;

import java.util.Optional;
import com.wargameclub.clubapi.entity.ClubTelegramSettings;
import com.wargameclub.clubapi.exception.NotFoundException;
import com.wargameclub.clubapi.repository.ClubTelegramSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис управления настройками Telegram для клуба.
 */
@Service
public class TelegramSettingsService {

    /**
     * Репозиторий настроек Telegram.
     */
    private final ClubTelegramSettingsRepository repository;

    /**
     * Создает сервис настроек Telegram.
     *
     * @param repository репозиторий настроек Telegram
     */
    public TelegramSettingsService(ClubTelegramSettingsRepository repository) {
        this.repository = repository;
    }

    /**
     * Возвращает любые существующие настройки Telegram (если есть).
     *
     * @return настройки Telegram
     */
    @Transactional(readOnly = true)
    public Optional<ClubTelegramSettings> getAny() {
        return repository.findFirstByOrderByChatIdAsc();
    }

    /**
     * Возвращает настройки Telegram по идентификатору чата.
     *
     * @param chatId идентификатор чата
     * @return настройки Telegram
     */
    @Transactional(readOnly = true)
    public ClubTelegramSettings getByChatId(Long chatId) {
        return repository.findByChatId(chatId)
                .orElseThrow(() -> new NotFoundException("Настройки Telegram не найдены для чата: " + chatId));
    }

    /**
     * Создает или обновляет настройки Telegram.
     *
     * @param chatId идентификатор чата
     * @param scheduleThreadId идентификатор темы расписания
     * @param eventsThreadId идентификатор темы мероприятий
     * @param scheduleTwoweeksMessageId идентификатор сообщения расписания на две недели
     * @param scheduleTwoweeksNextMessageId идентификатор следующего двухнедельного расписания
     * @param eventsMessageId идентификатор сообщения списка мероприятий
     * @param timezone часовой пояс
     * @return сохраненные настройки
     */
    @Transactional
    public ClubTelegramSettings upsert(
            Long chatId,
            Integer scheduleThreadId,
            Integer eventsThreadId,
            Integer scheduleTwoweeksMessageId,
            Integer scheduleTwoweeksNextMessageId,
            Integer eventsMessageId,
            String timezone
    ) {
        ClubTelegramSettings settings = repository.findByChatId(chatId)
                .orElseGet(() -> new ClubTelegramSettings(chatId));
        if (scheduleThreadId != null) {
            if (scheduleThreadId <= 0) {
                settings.setScheduleThreadId(null);
            } else {
                settings.setScheduleThreadId(scheduleThreadId);
            }
        }
        if (eventsThreadId != null) {
            if (eventsThreadId <= 0) {
                settings.setEventsThreadId(null);
            } else {
                settings.setEventsThreadId(eventsThreadId);
            }
        }
        if (scheduleTwoweeksMessageId != null) {
            settings.setScheduleTwoweeksMessageId(scheduleTwoweeksMessageId);
        }
        if (scheduleTwoweeksNextMessageId != null) {
            settings.setScheduleTwoweeksNextMessageId(scheduleTwoweeksNextMessageId);
        }
        if (eventsMessageId != null) {
            settings.setEventsMessageId(eventsMessageId);
        }
        if (timezone != null && !timezone.isBlank()) {
            settings.setTimezone(timezone);
        }
        return repository.save(settings);
    }
}
