package com.wargameclub.clubbot.config;

import com.wargameclub.clubbot.client.ApiJwtTokenProvider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

/**
 * Конфигурация клиента HTTP для взаимодействия с club-api.
 */
@Configuration
public class RestClientConfig {

    /**
     * Создает RestTemplate без auth-интерцепторов для получения JWT.
     *
     * @param builder билдер RestTemplate
     * @return RestTemplate для /api/auth/login
     */
    @Bean("apiAuthRestTemplate")
    public RestTemplate apiAuthRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    /**
     * Создает {@link RestTemplate} для HTTP-вызовов.
     *
     * @param builder билдер RestTemplate
     * @param apiProperties настройки доступа к API
     * @param tokenProvider поставщик JWT
     * @return настроенный RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            ApiProperties apiProperties,
            ApiJwtTokenProvider tokenProvider
    ) {
        return builder.additionalInterceptors((request, body, execution) -> {
            String apiKey = apiProperties.getApiKey();
            String authHeader = tokenProvider.getAuthorizationHeader();
            if (authHeader != null) {
                request.getHeaders().set(HttpHeaders.AUTHORIZATION, authHeader);
            } else if (apiKey != null && !apiKey.isBlank()) {
                request.getHeaders().set("X-API-KEY", apiKey);
            }

            ClientHttpResponse response = execution.execute(request, body);
            HttpStatusCode status = response.getStatusCode();
            if (status.value() != 401 || authHeader == null) {
                return response;
            }

            response.close();
            tokenProvider.invalidate();
            String refreshedHeader = tokenProvider.getAuthorizationHeader();
            if (refreshedHeader == null || refreshedHeader.equals(authHeader)) {
                return emptyUnauthorizedResponse();
            }

            request.getHeaders().set(HttpHeaders.AUTHORIZATION, refreshedHeader);
            return execution.execute(request, body);
        }).build();
    }

    /**
     * Возвращает пустой HTTP-ответ 401 для случая, когда refresh токена не дал нового значения.
     *
     * @return ответ со статусом 401
     */
    private ClientHttpResponse emptyUnauthorizedResponse() {
        return new ClientHttpResponse() {
            @Override
            public HttpStatusCode getStatusCode() {
                return HttpStatusCode.valueOf(401);
            }

            @Override
            public String getStatusText() {
                return "Unauthorized";
            }

            @Override
            public void close() {
            }

            @Override
            public java.io.InputStream getBody() {
                return java.io.InputStream.nullInputStream();
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }
        };
    }
}
