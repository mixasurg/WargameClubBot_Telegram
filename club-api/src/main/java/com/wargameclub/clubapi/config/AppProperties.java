package com.wargameclub.clubapi.config;

import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private ZoneId timezone = ZoneId.of("Europe/Moscow");
    private final Loyalty loyalty = new Loyalty();
    private final Notifications notifications = new Notifications();

    public ZoneId getTimezone() {
        return timezone;
    }

    public void setTimezone(ZoneId timezone) {
        this.timezone = timezone;
    }

    public Loyalty getLoyalty() {
        return loyalty;
    }

    public Notifications getNotifications() {
        return notifications;
    }

    public static class Loyalty {
        private int pointsArmyUsed = 10;
        private int pointsArmyShared = 5;

        public int getPointsArmyUsed() {
            return pointsArmyUsed;
        }

        public void setPointsArmyUsed(int pointsArmyUsed) {
            this.pointsArmyUsed = pointsArmyUsed;
        }

        public int getPointsArmyShared() {
            return pointsArmyShared;
        }

        public void setPointsArmyShared(int pointsArmyShared) {
            this.pointsArmyShared = pointsArmyShared;
        }
    }

    public static class Notifications {
        private int maxAttempts = 5;
        private int backoffSeconds = 30;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public int getBackoffSeconds() {
            return backoffSeconds;
        }

        public void setBackoffSeconds(int backoffSeconds) {
            this.backoffSeconds = backoffSeconds;
        }
    }
}

