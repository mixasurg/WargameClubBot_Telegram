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
         * API-ключ для защищенных эндпоинтов.
         */
        private String apiKey;

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
