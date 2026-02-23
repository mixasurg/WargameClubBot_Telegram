package com.wargameclub.clubapi.config;

import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурация для App.
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
     * Возвращает Timezone.
     */
    public ZoneId getTimezone() {
        return timezone;
    }

    /**
     * Устанавливает Timezone.
     */
    public void setTimezone(ZoneId timezone) {
        this.timezone = timezone;
    }

    /**
     * Возвращает лояльность.
     */
    public Loyalty getLoyalty() {
        return loyalty;
    }

    /**
     * Возвращает Notifications.
     */
    public Notifications getNotifications() {
        return notifications;
    }

    /**
     * Конфигурация для лояльности.
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
         * Возвращает PointsArmyUsed.
         */
        public int getPointsArmyUsed() {
            return pointsArmyUsed;
        }

        /**
         * Устанавливает PointsArmyUsed.
         */
        public void setPointsArmyUsed(int pointsArmyUsed) {
            this.pointsArmyUsed = pointsArmyUsed;
        }

        /**
         * Возвращает PointsArmyShared.
         */
        public int getPointsArmyShared() {
            return pointsArmyShared;
        }

        /**
         * Устанавливает PointsArmyShared.
         */
        public void setPointsArmyShared(int pointsArmyShared) {
            this.pointsArmyShared = pointsArmyShared;
        }
    }

    /**
     * Конфигурация для Notifications.
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
         * Возвращает MaxAttempts.
         */
        public int getMaxAttempts() {
            return maxAttempts;
        }

        /**
         * Устанавливает MaxAttempts.
         */
        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        /**
         * Возвращает BackoffSeconds.
         */
        public int getBackoffSeconds() {
            return backoffSeconds;
        }

        /**
         * Устанавливает BackoffSeconds.
         */
        public void setBackoffSeconds(int backoffSeconds) {
            this.backoffSeconds = backoffSeconds;
        }
    }
}

