package com.wargameclub.clubapi.controller;

import com.wargameclub.clubapi.dto.TelegramSettingsDto;
import com.wargameclub.clubapi.dto.TelegramSettingsUpdateRequest;
import com.wargameclub.clubapi.entity.ClubTelegramSettings;
import com.wargameclub.clubapi.service.TelegramSettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для управления настройками Telegram.
 */
@RestController
@RequestMapping("/api/telegram/settings")
public class TelegramSettingsController {

    /**
     * Сервис настроек Telegram.
     */
    private final TelegramSettingsService settingsService;

    /**
     * Конструктор TelegramSettingsController.
     */
    public TelegramSettingsController(TelegramSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /**
     * Возвращает настройки Telegram.
     */
    @GetMapping
    public TelegramSettingsDto get() {
        ClubTelegramSettings settings = settingsService.getAny()
                .orElseThrow(() -> new com.wargameclub.clubapi.exception.NotFoundException("Настройки Telegram не заданы"));
        return toDto(settings);
    }

    /**
     * Обновляет настройки Telegram.
     */
    @PutMapping
    public TelegramSettingsDto update(@Valid @RequestBody TelegramSettingsUpdateRequest request) {
        ClubTelegramSettings settings = settingsService.upsert(
                request.chatId(),
                request.scheduleThreadId(),
                request.eventsThreadId(),
                request.scheduleTwoweeksMessageId(),
                request.scheduleTwoweeksNextMessageId(),
                request.eventsMessageId(),
                request.timezone()
        );
        return toDto(settings);
    }

    /**
     * Преобразует в DTO.
     */
    private TelegramSettingsDto toDto(ClubTelegramSettings settings) {
        return new TelegramSettingsDto(
                settings.getChatId(),
                settings.getScheduleThreadId(),
                settings.getEventsThreadId(),
                settings.getScheduleTwoweeksMessageId(),
                settings.getScheduleTwoweeksNextMessageId(),
                settings.getEventsMessageId(),
                settings.getTimezone()
        );
    }
}

