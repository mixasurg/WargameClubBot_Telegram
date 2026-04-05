package com.wargameclub.clubbot.client;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import com.wargameclub.clubbot.config.ApiProperties;
import com.wargameclub.clubbot.dto.AuthLoginRequest;
import com.wargameclub.clubbot.dto.AuthTokenResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Поставщик JWT-токена для запросов club-bot -> club-api.
 */
@Component
public class ApiJwtTokenProvider {

    /**
     * RestTemplate без auth-интерцепторов (используется только для входа).
     */
    private final RestTemplate authRestTemplate;

    /**
     * Настройки API.
     */
    private final ApiProperties apiProperties;

    /**
     * Текущий кэшированный токен.
     */
    private volatile CachedToken cachedToken;

    /**
     * Создает поставщика JWT.
     *
     * @param authRestTemplate RestTemplate для login-запроса
     * @param apiProperties настройки API
     */
    public ApiJwtTokenProvider(
            @Qualifier("apiAuthRestTemplate") RestTemplate authRestTemplate,
            ApiProperties apiProperties
    ) {
        this.authRestTemplate = authRestTemplate;
        this.apiProperties = apiProperties;
    }

    /**
     * Возвращает Authorization-заголовок в формате "Bearer ...".
     *
     * @return значение Authorization или null, если JWT-режим не настроен
     */
    public String getAuthorizationHeader() {
        if (!hasCredentials()) {
            return null;
        }
        CachedToken current = cachedToken;
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (current != null && !current.isExpiringSoon(now, apiProperties.getAuthRefreshSkewSeconds())) {
            return current.authorizationHeader();
        }
        synchronized (this) {
            current = cachedToken;
            now = OffsetDateTime.now(ZoneOffset.UTC);
            if (current != null && !current.isExpiringSoon(now, apiProperties.getAuthRefreshSkewSeconds())) {
                return current.authorizationHeader();
            }
            return loginAndCache().authorizationHeader();
        }
    }

    /**
     * Сбрасывает кэшированный токен (используется при 401 от API).
     */
    public synchronized void invalidate() {
        cachedToken = null;
    }

    /**
     * Выполняет login в API и обновляет кэш токена.
     *
     * @return кэшированная запись токена
     */
    private CachedToken loginAndCache() {
        AuthLoginRequest request = new AuthLoginRequest(
                apiProperties.getLogin(),
                apiProperties.getPassword()
        );
        AuthTokenResponse response = authRestTemplate.postForObject(
                apiProperties.getBaseUrl() + "/api/auth/login",
                request,
                AuthTokenResponse.class
        );
        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new IllegalStateException("club-api не вернул JWT-токен");
        }
        String tokenType = response.tokenType() == null || response.tokenType().isBlank()
                ? "Bearer"
                : response.tokenType().trim();
        OffsetDateTime expiresAt = response.expiresAt() == null
                ? OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(30)
                : response.expiresAt();
        CachedToken token = new CachedToken(tokenType + " " + response.accessToken(), expiresAt);
        cachedToken = token;
        return token;
    }

    /**
     * Проверяет, задана ли пара login/password для JWT-аутентификации.
     *
     * @return true, если JWT-режим настроен
     */
    private boolean hasCredentials() {
        return apiProperties.getLogin() != null && !apiProperties.getLogin().isBlank()
                && apiProperties.getPassword() != null && !apiProperties.getPassword().isBlank();
    }

    /**
     * Кэш JWT-токена.
     *
     * @param authorizationHeader значение заголовка Authorization
     * @param expiresAt время истечения токена
     */
    private record CachedToken(String authorizationHeader, OffsetDateTime expiresAt) {

        /**
         * Проверяет, истекает ли токен в пределах заданного окна.
         *
         * @param now текущее время
         * @param skewSeconds допустимый запас в секундах
         * @return true, если пора обновлять токен
         */
        boolean isExpiringSoon(OffsetDateTime now, long skewSeconds) {
            long safeSkew = Math.max(0L, skewSeconds);
            return !expiresAt.isAfter(now.plusSeconds(safeSkew));
        }
    }
}
