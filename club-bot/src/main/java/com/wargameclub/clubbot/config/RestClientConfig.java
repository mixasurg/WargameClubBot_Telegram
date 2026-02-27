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
     * @param apiProperties настройки доступа к API
     * @return настроенный RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, ApiProperties apiProperties) {
        String apiKey = apiProperties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return builder.build();
        }
        return builder.additionalInterceptors((request, body, execution) -> {
            request.getHeaders().add("X-API-KEY", apiKey);
            return execution.execute(request, body);
        }).build();
    }
}
