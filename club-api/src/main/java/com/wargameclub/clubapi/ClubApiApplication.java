package com.wargameclub.clubapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Точка входа приложения club-api.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class ClubApiApplication {

    /**
     * Выполняет операцию.
     */
    public static void main(String[] args) {
        SpringApplication.run(ClubApiApplication.class, args);
    }
}
