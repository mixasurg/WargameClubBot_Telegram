package com.wargameclub.clubapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Точка входа приложения club-api.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ClubApiApplication {

    /**
     * Выполняет операцию.
     */
    public static void main(String[] args) {
        SpringApplication.run(ClubApiApplication.class, args);
    }
}

