package com.wargameclub.clubapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA-сущность настроек Telegram для клуба.
 */
@Entity
@Table(name = "club_telegram_settings")
public class ClubTelegramSettings {

    /**
     * Идентификатор чата Telegram.
     */
    @Id
    @Column(name = "chat_id")
    private Long chatId;

    /**
     * Идентификатор темы расписания.
     */
    @Column(name = "schedule_thread_id")
    private Integer scheduleThreadId;

    /**
     * Идентификатор темы мероприятий.
     */
    @Column(name = "events_thread_id")
    private Integer eventsThreadId;

    /**
     * Идентификатор сообщения расписания на две недели.
     */
    @Column(name = "schedule_twoweeks_message_id")
    private Integer scheduleTwoweeksMessageId;

    /**
     * Идентификатор сообщения следующего двухнедельного расписания.
     */
    @Column(name = "schedule_twoweeks_next_message_id")
    private Integer scheduleTwoweeksNextMessageId;

    /**
     * Идентификатор сообщения списка мероприятий.
     */
    @Column(name = "events_message_id")
    private Integer eventsMessageId;

    /**
     * Часовой пояс для публикации сообщений.
     */
    @Column(nullable = false, length = 50)
    private String timezone = "Europe/Moscow";

    /**
     * Создает пустую сущность для JPA.
     */
    public ClubTelegramSettings() {
    }

    /**
     * Создает настройки Telegram для указанного чата.
     *
     * @param chatId идентификатор чата
     */
    public ClubTelegramSettings(Long chatId) {
        this.chatId = chatId;
        this.timezone = "Europe/Moscow";
    }

    /**
     * Возвращает идентификатор чата.
     *
     * @return идентификатор чата
     */
    public Long getChatId() {
        return chatId;
    }

    /**
     * Устанавливает идентификатор чата.
     *
     * @param chatId идентификатор чата
     */
    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    /**
     * Возвращает идентификатор темы расписания.
     *
     * @return идентификатор темы расписания
     */
    public Integer getScheduleThreadId() {
        return scheduleThreadId;
    }

    /**
     * Устанавливает идентификатор темы расписания.
     *
     * @param scheduleThreadId идентификатор темы расписания
     */
    public void setScheduleThreadId(Integer scheduleThreadId) {
        this.scheduleThreadId = scheduleThreadId;
    }

    /**
     * Возвращает идентификатор темы мероприятий.
     *
     * @return идентификатор темы мероприятий
     */
    public Integer getEventsThreadId() {
        return eventsThreadId;
    }

    /**
     * Устанавливает идентификатор темы мероприятий.
     *
     * @param eventsThreadId идентификатор темы мероприятий
     */
    public void setEventsThreadId(Integer eventsThreadId) {
        this.eventsThreadId = eventsThreadId;
    }

    /**
     * Возвращает идентификатор сообщения расписания на две недели.
     *
     * @return идентификатор сообщения расписания
     */
    public Integer getScheduleTwoweeksMessageId() {
        return scheduleTwoweeksMessageId;
    }

    /**
     * Устанавливает идентификатор сообщения расписания на две недели.
     *
     * @param scheduleTwoweeksMessageId идентификатор сообщения расписания
     */
    public void setScheduleTwoweeksMessageId(Integer scheduleTwoweeksMessageId) {
        this.scheduleTwoweeksMessageId = scheduleTwoweeksMessageId;
    }

    /**
     * Возвращает идентификатор сообщения следующего двухнедельного расписания.
     *
     * @return идентификатор сообщения следующего расписания
     */
    public Integer getScheduleTwoweeksNextMessageId() {
        return scheduleTwoweeksNextMessageId;
    }

    /**
     * Устанавливает идентификатор сообщения следующего двухнедельного расписания.
     *
     * @param scheduleTwoweeksNextMessageId идентификатор сообщения следующего расписания
     */
    public void setScheduleTwoweeksNextMessageId(Integer scheduleTwoweeksNextMessageId) {
        this.scheduleTwoweeksNextMessageId = scheduleTwoweeksNextMessageId;
    }

    /**
     * Возвращает идентификатор сообщения списка мероприятий.
     *
     * @return идентификатор сообщения мероприятий
     */
    public Integer getEventsMessageId() {
        return eventsMessageId;
    }

    /**
     * Устанавливает идентификатор сообщения списка мероприятий.
     *
     * @param eventsMessageId идентификатор сообщения мероприятий
     */
    public void setEventsMessageId(Integer eventsMessageId) {
        this.eventsMessageId = eventsMessageId;
    }

    /**
     * Возвращает часовой пояс.
     *
     * @return часовой пояс
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Устанавливает часовой пояс.
     *
     * @param timezone часовой пояс
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
