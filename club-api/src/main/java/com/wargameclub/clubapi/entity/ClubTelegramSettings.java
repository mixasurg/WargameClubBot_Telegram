package com.wargameclub.clubapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "club_telegram_settings")
public class ClubTelegramSettings {
    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "schedule_thread_id")
    private Integer scheduleThreadId;

    @Column(name = "events_thread_id")
    private Integer eventsThreadId;

    @Column(name = "schedule_twoweeks_message_id")
    private Integer scheduleTwoweeksMessageId;

    @Column(name = "schedule_twoweeks_next_message_id")
    private Integer scheduleTwoweeksNextMessageId;

    @Column(name = "events_message_id")
    private Integer eventsMessageId;

    @Column(nullable = false, length = 50)
    private String timezone = "Europe/Moscow";

    public ClubTelegramSettings() {
    }

    public ClubTelegramSettings(Long chatId) {
        this.chatId = chatId;
        this.timezone = "Europe/Moscow";
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Integer getScheduleThreadId() {
        return scheduleThreadId;
    }

    public void setScheduleThreadId(Integer scheduleThreadId) {
        this.scheduleThreadId = scheduleThreadId;
    }

    public Integer getEventsThreadId() {
        return eventsThreadId;
    }

    public void setEventsThreadId(Integer eventsThreadId) {
        this.eventsThreadId = eventsThreadId;
    }

    public Integer getScheduleTwoweeksMessageId() {
        return scheduleTwoweeksMessageId;
    }

    public void setScheduleTwoweeksMessageId(Integer scheduleTwoweeksMessageId) {
        this.scheduleTwoweeksMessageId = scheduleTwoweeksMessageId;
    }

    public Integer getScheduleTwoweeksNextMessageId() {
        return scheduleTwoweeksNextMessageId;
    }

    public void setScheduleTwoweeksNextMessageId(Integer scheduleTwoweeksNextMessageId) {
        this.scheduleTwoweeksNextMessageId = scheduleTwoweeksNextMessageId;
    }

    public Integer getEventsMessageId() {
        return eventsMessageId;
    }

    public void setEventsMessageId(Integer eventsMessageId) {
        this.eventsMessageId = eventsMessageId;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
