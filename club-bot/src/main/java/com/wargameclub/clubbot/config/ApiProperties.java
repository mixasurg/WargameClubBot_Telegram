package com.wargameclub.clubbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки доступа к club-api.
 */
@ConfigurationProperties(prefix = "api")
public class ApiProperties {

    /**
     * Базовый URL club-api.
     */
    private String baseUrl = "http://localhost:8080";

    /**
     * Возвращает базовый URL club-api.
     *
     * @return базовый URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Устанавливает базовый URL club-api.
     *
     * @param baseUrl базовый URL
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
