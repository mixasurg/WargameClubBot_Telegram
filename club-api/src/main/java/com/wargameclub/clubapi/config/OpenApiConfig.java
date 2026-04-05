package com.wargameclub.clubapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI/Swagger с базовыми метаданными сервиса.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Создает {@link OpenAPI} с метаданными API клуба: название, версия и описание.
     *
     * @return объект OpenAPI для публикации спецификации Swagger/OpenAPI
     */
    @Bean
    public OpenAPI clubOpenApi() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .info(new Info()
                        .title("API клуба Wargame")
                        .version("1.0.0")
                        .description("REST API для расписания клуба, мероприятий, армий и уведомлений."));
    }
}
