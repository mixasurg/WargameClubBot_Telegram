package com.wargameclub.clubapi.enums;

/**
 * Роли пользователей в модели RBAC.
 */
public enum UserRole {
    /**
     * Администратор системы.
     */
    ADMIN,
    /**
     * Организатор мероприятий.
     */
    ORGANIZER,
    /**
     * Обычный участник клуба.
     */
    MEMBER,
    /**
     * Сервисный пользователь Telegram-бота.
     */
    BOT_SERVICE
}
