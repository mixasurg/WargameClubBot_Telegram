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
     * Запускает Spring Boot приложение `club-api`.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(ClubApiApplication.class, args);
    }
}
