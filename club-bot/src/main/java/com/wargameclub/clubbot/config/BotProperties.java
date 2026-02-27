package com.wargameclub.clubbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки Telegram-бота.
 */
@ConfigurationProperties(prefix = "bot")
public class BotProperties {

    /**
     * Токен Telegram-бота.
     */
    private String token;

    /**
     * Username Telegram-бота.
     */
    private String username = "wargameclub_bot";

    /**
     * Интервал опроса обновлений в секундах.
     */
    private int pollIntervalSeconds = 10;

    /**
     * Возвращает токен бота.
     *
     * @return токен бота
     */
    public String getToken() {
        return token;
    }

    /**
     * Устанавливает токен бота.
     *
     * @param token токен бота
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Возвращает username бота.
     *
     * @return username бота
     */
    public String getUsername() {
        return username;
    }

    /**
     * Устанавливает username бота.
     *
     * @param username username бота
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Возвращает интервал опроса в секундах.
     *
     * @return интервал опроса
     */
    public int getPollIntervalSeconds() {
        return pollIntervalSeconds;
    }

    /**
     * Устанавливает интервал опроса в секундах.
     *
     * @param pollIntervalSeconds интервал опроса
     */
    public void setPollIntervalSeconds(int pollIntervalSeconds) {
        this.pollIntervalSeconds = pollIntervalSeconds;
    }
}
