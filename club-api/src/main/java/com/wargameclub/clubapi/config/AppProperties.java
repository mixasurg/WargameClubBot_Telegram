package com.wargameclub.clubapi.config;

import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Типизированные настройки приложения, загружаемые из префикса {@code app.*}.
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    /**
     * Часовой пояс приложения по умолчанию.
     */
    private ZoneId timezone = ZoneId.of("Europe/Moscow");
    /**
     * Настройки безопасности API.
     */
    private final Security security = new Security();
    /**
     * Настройки начисления баллов лояльности.
     */
    private final Loyalty loyalty = new Loyalty();
    /**
     * Настройки ретраев уведомлений.
     */
    private final Notifications notifications = new Notifications();

    /**
     * Возвращает часовой пояс, используемый приложением по умолчанию.
     *
     * @return часовой пояс приложения
     */
    public ZoneId getTimezone() {
        return timezone;
    }

    /**
     * Устанавливает часовой пояс, используемый приложением по умолчанию.
     *
     * @param timezone часовой пояс приложения
     */
    public void setTimezone(ZoneId timezone) {
        this.timezone = timezone;
    }

    /**
     * Возвращает настройки безопасности.
     *
     * @return настройки безопасности
     */
    public Security getSecurity() {
        return security;
    }

    /**
     * Возвращает настройки лояльности.
     *
     * @return конфигурация начисления баллов
     */
    public Loyalty getLoyalty() {
        return loyalty;
    }

    /**
     * Возвращает настройки повторных попыток уведомлений.
     *
     * @return конфигурация уведомлений
     */
    public Notifications getNotifications() {
        return notifications;
    }

    /**
     * Настройки безопасности приложения.
     */
    public static class Security {

        /**
         * Флаг включения security-цепочки (JWT + RBAC).
         */
        private boolean enabled = true;

        /**
         * API-ключ для защищенных эндпоинтов.
         */
        private String apiKey;

        /**
         * Секрет для подписи JWT-токенов.
         */
        private String jwtSecret = "change-me-please-change-me-please-12345";

        /**
         * Время жизни JWT-токена в секундах.
         */
        private long jwtTtlSeconds = 3600;

        /**
         * Включение ограничения частоты запросов.
         */
        private boolean rateLimitEnabled = true;

        /**
         * Максимум запросов в минуту для обычных API-эндпоинтов.
         */
        private int rateLimitPerMinute = 120;

        /**
         * Максимум запросов в минуту для эндпоинта login.
         */
        private int loginRateLimitPerMinute = 20;

        /**
         * Возвращает флаг включения security-цепочки.
         *
         * @return true, если security включена
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Устанавливает флаг включения security-цепочки.
         *
         * @param enabled признак включения security
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Возвращает API-ключ для доступа к защищенным эндпоинтам.
         *
         * @return API-ключ или null
         */
        public String getApiKey() {
            return apiKey;
        }

        /**
         * Устанавливает API-ключ для доступа к защищенным эндпоинтам.
         *
         * @param apiKey API-ключ
         */
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        /**
         * Возвращает JWT-секрет.
         *
         * @return JWT-секрет
         */
        public String getJwtSecret() {
            return jwtSecret;
        }

        /**
         * Устанавливает JWT-секрет.
         *
         * @param jwtSecret JWT-секрет
         */
        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        /**
         * Возвращает время жизни JWT-токена в секундах.
         *
         * @return TTL токена в секундах
         */
        public long getJwtTtlSeconds() {
            return jwtTtlSeconds;
        }

        /**
         * Устанавливает время жизни JWT-токена в секундах.
         *
         * @param jwtTtlSeconds TTL токена в секундах
         */
        public void setJwtTtlSeconds(long jwtTtlSeconds) {
            this.jwtTtlSeconds = jwtTtlSeconds;
        }

        /**
         * Возвращает флаг включения rate limiting.
         *
         * @return true, если ограничение частоты включено
         */
        public boolean isRateLimitEnabled() {
            return rateLimitEnabled;
        }

        /**
         * Устанавливает флаг включения rate limiting.
         *
         * @param rateLimitEnabled признак включения ограничения
         */
        public void setRateLimitEnabled(boolean rateLimitEnabled) {
            this.rateLimitEnabled = rateLimitEnabled;
        }

        /**
         * Возвращает лимит запросов в минуту для обычных API-эндпоинтов.
         *
         * @return лимит запросов
         */
        public int getRateLimitPerMinute() {
            return rateLimitPerMinute;
        }

        /**
         * Устанавливает лимит запросов в минуту для обычных API-эндпоинтов.
         *
         * @param rateLimitPerMinute лимит запросов
         */
        public void setRateLimitPerMinute(int rateLimitPerMinute) {
            this.rateLimitPerMinute = rateLimitPerMinute;
        }

        /**
         * Возвращает лимит запросов в минуту для login-эндпоинта.
         *
         * @return лимит login-запросов
         */
        public int getLoginRateLimitPerMinute() {
            return loginRateLimitPerMinute;
        }

        /**
         * Устанавливает лимит запросов в минуту для login-эндпоинта.
         *
         * @param loginRateLimitPerMinute лимит login-запросов
         */
        public void setLoginRateLimitPerMinute(int loginRateLimitPerMinute) {
            this.loginRateLimitPerMinute = loginRateLimitPerMinute;
        }
    }

    /**
     * Настройки начисления баллов лояльности за действия пользователей.
     */
    public static class Loyalty {

        /**
         * Баллы за использование армии.
         */
        private int pointsArmyUsed = 10;

        /**
         * Баллы за предоставление армии клубу.
         */
        private int pointsArmyShared = 5;

        /**
         * Возвращает количество баллов за использование армии.
         *
         * @return баллы за использование армии
         */
        public int getPointsArmyUsed() {
            return pointsArmyUsed;
        }

        /**
         * Устанавливает количество баллов за использование армии.
         *
         * @param pointsArmyUsed баллы за использование армии
         */
        public void setPointsArmyUsed(int pointsArmyUsed) {
            this.pointsArmyUsed = pointsArmyUsed;
        }

        /**
         * Возвращает количество баллов за шаринг армии.
         *
         * @return баллы за шаринг армии
         */
        public int getPointsArmyShared() {
            return pointsArmyShared;
        }

        /**
         * Устанавливает количество баллов за шаринг армии.
         *
         * @param pointsArmyShared баллы за шаринг армии
         */
        public void setPointsArmyShared(int pointsArmyShared) {
            this.pointsArmyShared = pointsArmyShared;
        }
    }

    /**
     * Настройки повторных попыток отправки уведомлений.
     */
    public static class Notifications {

        /**
         * Максимальное число попыток отправки уведомления.
         */
        private int maxAttempts = 5;

        /**
         * Задержка между попытками отправки в секундах.
         */
        private int backoffSeconds = 30;

        /**
         * Возвращает максимальное число попыток отправки уведомления.
         *
         * @return максимальное число попыток
         */
        public int getMaxAttempts() {
            return maxAttempts;
        }

        /**
         * Устанавливает максимальное число попыток отправки уведомления.
         *
         * @param maxAttempts максимальное число попыток
         */
        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        /**
         * Возвращает задержку между попытками отправки уведомления в секундах.
         *
         * @return задержка между попытками в секундах
         */
        public int getBackoffSeconds() {
            return backoffSeconds;
        }

        /**
         * Устанавливает задержку между попытками отправки уведомления в секундах.
         *
         * @param backoffSeconds задержка между попытками в секундах
         */
        public void setBackoffSeconds(int backoffSeconds) {
            this.backoffSeconds = backoffSeconds;
        }
    }
}
