package com.wargameclub.clubapi.enums;

/**
 * Статусы регистрации на мероприятие.
 */
public enum RegistrationStatus {
    /**
     * Пользователь зарегистрирован.
     */
    REGISTERED,
    /**
     * Пользователь подтвердил, что придет.
     */
    CONFIRMED,
    /**
     * Регистрация отменена.
     */
    CANCELLED
}
