package com.wargameclub.clubapi.config;

import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Типизированные настройки приложения, загружаемые из префикса {@code app.*}.
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    /**
     * Поле состояния.
     */
    private ZoneId timezone = ZoneId.of("Europe/Moscow");
    /**
     * Поле состояния.
     */
    private final Loyalty loyalty = new Loyalty();
    /**
     * Поле состояния.
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
     * Настройки начисления баллов лояльности за действия пользователей.
     */
    public static class Loyalty {

        /**
         * Поле состояния.
         */
        private int pointsArmyUsed = 10;

        /**
         * Поле состояния.
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
         * Поле состояния.
         */
        private int maxAttempts = 5;

        /**
         * Поле состояния.
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
