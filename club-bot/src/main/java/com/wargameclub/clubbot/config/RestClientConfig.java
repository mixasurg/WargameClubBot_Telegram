package com.wargameclub.clubbot.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Конфигурация клиента HTTP для взаимодействия с club-api.
 */
@Configuration
public class RestClientConfig {

    /**
     * Создает {@link RestTemplate} для HTTP-вызовов.
     *
     * @param builder билдер RestTemplate
     * @return настроенный RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
