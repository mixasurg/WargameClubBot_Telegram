package com.wargameclub.clubapi.service;

import java.util.Optional;
import com.wargameclub.clubapi.entity.ClubTelegramSettings;
import com.wargameclub.clubapi.exception.NotFoundException;
import com.wargameclub.clubapi.repository.ClubTelegramSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TelegramSettingsService {
    private final ClubTelegramSettingsRepository repository;

    public TelegramSettingsService(ClubTelegramSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<ClubTelegramSettings> getAny() {
        return repository.findFirstByOrderByChatIdAsc();
    }

    @Transactional(readOnly = true)
    public ClubTelegramSettings getByChatId(Long chatId) {
        return repository.findByChatId(chatId)
                .orElseThrow(() -> new NotFoundException("Настройки Telegram не найдены для чата: " + chatId));
    }

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

