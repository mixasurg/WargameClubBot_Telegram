package com.wargameclub.clubbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурация для бота.
 */
@ConfigurationProperties(prefix = "bot")
public class BotProperties {

    /**
     * Поле состояния.
     */
    private String token;

    /**
     * Поле состояния.
     */
    private String username = "wargameclub_bot";

    /**
     * Поле состояния.
     */
    private int pollIntervalSeconds = 10;

    /**
     * Возвращает Token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Устанавливает Token.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Возвращает Username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Устанавливает Username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Возвращает PollIntervalSeconds.
     */
    public int getPollIntervalSeconds() {
        return pollIntervalSeconds;
    }

    /**
     * Устанавливает PollIntervalSeconds.
     */
    public void setPollIntervalSeconds(int pollIntervalSeconds) {
        this.pollIntervalSeconds = pollIntervalSeconds;
    }
}

