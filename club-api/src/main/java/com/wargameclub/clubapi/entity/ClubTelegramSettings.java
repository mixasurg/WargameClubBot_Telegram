package com.wargameclub.clubapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA-сущность ClubTelegram.
 */
@Entity
@Table(name = "club_telegram_settings")
public class ClubTelegramSettings {

    /**
     * Поле состояния.
     */
    @Id
    @Column(name = "chat_id")
    private Long chatId;

    /**
     * Поле состояния.
     */
    @Column(name = "schedule_thread_id")
    private Integer scheduleThreadId;

    /**
     * Поле состояния.
     */
    @Column(name = "events_thread_id")
    private Integer eventsThreadId;

    /**
     * Поле состояния.
     */
    @Column(name = "schedule_twoweeks_message_id")
    private Integer scheduleTwoweeksMessageId;

    /**
     * Поле состояния.
     */
    @Column(name = "schedule_twoweeks_next_message_id")
    private Integer scheduleTwoweeksNextMessageId;

    /**
     * Поле состояния.
     */
    @Column(name = "events_message_id")
    private Integer eventsMessageId;

    /**
     * Поле состояния.
     */
    @Column(nullable = false, length = 50)
    private String timezone = "Europe/Moscow";

    /**
     * Конструктор ClubTelegramSettings.
     */
    public ClubTelegramSettings() {
    }

    /**
     * Конструктор ClubTelegramSettings.
     */
    public ClubTelegramSettings(Long chatId) {
        this.chatId = chatId;
        this.timezone = "Europe/Moscow";
    }

    /**
     * Возвращает идентификатор Chat.
     */
    public Long getChatId() {
        return chatId;
    }

    /**
     * Устанавливает идентификатор Chat.
     */
    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    /**
     * Возвращает идентификатор ScheduleThread.
     */
    public Integer getScheduleThreadId() {
        return scheduleThreadId;
    }

    /**
     * Устанавливает идентификатор ScheduleThread.
     */
    public void setScheduleThreadId(Integer scheduleThreadId) {
        this.scheduleThreadId = scheduleThreadId;
    }

    /**
     * Возвращает идентификатор EventsThread.
     */
    public Integer getEventsThreadId() {
        return eventsThreadId;
    }

    /**
     * Устанавливает идентификатор EventsThread.
     */
    public void setEventsThreadId(Integer eventsThreadId) {
        this.eventsThreadId = eventsThreadId;
    }

    /**
     * Возвращает идентификатор ScheduleTwoweeksMessage.
     */
    public Integer getScheduleTwoweeksMessageId() {
        return scheduleTwoweeksMessageId;
    }

    /**
     * Устанавливает идентификатор ScheduleTwoweeksMessage.
     */
    public void setScheduleTwoweeksMessageId(Integer scheduleTwoweeksMessageId) {
        this.scheduleTwoweeksMessageId = scheduleTwoweeksMessageId;
    }

    /**
     * Возвращает идентификатор ScheduleTwoweeksNextMessage.
     */
    public Integer getScheduleTwoweeksNextMessageId() {
        return scheduleTwoweeksNextMessageId;
    }

    /**
     * Устанавливает идентификатор ScheduleTwoweeksNextMessage.
     */
    public void setScheduleTwoweeksNextMessageId(Integer scheduleTwoweeksNextMessageId) {
        this.scheduleTwoweeksNextMessageId = scheduleTwoweeksNextMessageId;
    }

    /**
     * Возвращает идентификатор EventsMessage.
     */
    public Integer getEventsMessageId() {
        return eventsMessageId;
    }

    /**
     * Устанавливает идентификатор EventsMessage.
     */
    public void setEventsMessageId(Integer eventsMessageId) {
        this.eventsMessageId = eventsMessageId;
    }

    /**
     * Возвращает Timezone.
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Устанавливает Timezone.
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
