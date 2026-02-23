package com.wargameclub.clubapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для OpenApi.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Выполняет операцию.
     */
    @Bean
    public OpenAPI clubOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API клуба Wargame")
                        .version("1.0.0")
                        .description("REST API для расписания клуба, мероприятий, армий и уведомлений."));
    }
}

