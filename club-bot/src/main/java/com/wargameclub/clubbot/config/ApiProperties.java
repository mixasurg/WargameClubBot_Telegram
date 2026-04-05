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
     * API-ключ для авторизации запросов.
     */
    private String apiKey;

    /**
     * Логин сервисного пользователя для получения JWT.
     */
    private String login = "club_bot";

    /**
     * Пароль сервисного пользователя для получения JWT.
     */
    private String password = "club_bot_pass";

    /**
     * Допустимый запас до истечения токена, когда нужен принудительный refresh.
     */
    private long authRefreshSkewSeconds = 30;

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

    /**
     * Возвращает API-ключ для авторизации запросов.
     *
     * @return API-ключ или null
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Устанавливает API-ключ для авторизации запросов.
     *
     * @param apiKey API-ключ
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Возвращает логин сервисного пользователя.
     *
     * @return логин сервиса
     */
    public String getLogin() {
        return login;
    }

    /**
     * Устанавливает логин сервисного пользователя.
     *
     * @param login логин сервиса
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Возвращает пароль сервисного пользователя.
     *
     * @return пароль сервиса
     */
    public String getPassword() {
        return password;
    }

    /**
     * Устанавливает пароль сервисного пользователя.
     *
     * @param password пароль сервиса
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Возвращает запас в секундах до истечения JWT, при котором нужно обновить токен.
     *
     * @return запас в секундах
     */
    public long getAuthRefreshSkewSeconds() {
        return authRefreshSkewSeconds;
    }

    /**
     * Устанавливает запас в секундах до истечения JWT для refresh.
     *
     * @param authRefreshSkewSeconds запас в секундах
     */
    public void setAuthRefreshSkewSeconds(long authRefreshSkewSeconds) {
        this.authRefreshSkewSeconds = authRefreshSkewSeconds;
    }

    /**
     * Обратная совместимость: поддержка legacy-свойства {@code api.key}.
     *
     * @param key API-ключ
     */
    public void setKey(String key) {
        this.apiKey = key;
    }
}
