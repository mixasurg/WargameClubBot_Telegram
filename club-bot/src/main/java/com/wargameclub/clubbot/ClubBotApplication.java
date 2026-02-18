package com.wargameclub.clubbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.wargameclub.clubbot.config.BotProperties;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
@EnableConfigurationProperties(BotProperties.class)
public class ClubBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClubBotApplication.class, args);
    }
}

