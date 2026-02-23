package com.wargameclub.clubbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурация для Api.
 */
@ConfigurationProperties(prefix = "api")
public class ApiProperties {

    /**
     * Поле состояния.
     */
    private String baseUrl = "http://localhost:8080";

    /**
     * Возвращает BaseUrl.
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Устанавливает BaseUrl.
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}

