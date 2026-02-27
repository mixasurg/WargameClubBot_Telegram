package com.wargameclub.clubapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Конфигурация логирования входящих HTTP-запросов через {@link CommonsRequestLoggingFilter}.
 */
@Configuration
public class RequestLoggingConfig {

    /**
     * Создает фильтр логирования HTTP-запросов и настраивает формат записи.
     *
     * @return фильтр логирования входящих запросов
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false);
        filter.setMaxPayloadLength(1000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("HTTP запрос: ");
        return filter;
    }
}
