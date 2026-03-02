package com.wargameclub.clubapi.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.wargameclub.clubapi.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Фильтр, проверяющий API-ключ для защищенных эндпоинтов.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiKeyFilter extends OncePerRequestFilter {

    /**
     * HTTP-заголовок с API-ключом.
     */
    private static final String API_KEY_HEADER = "X-API-KEY";

    /**
     * Настройки приложения.
     */
    private final AppProperties appProperties;

    /**
     * Создает фильтр проверки API-ключа.
     *
     * @param appProperties настройки приложения
     */
    public ApiKeyFilter(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * Проверяет API-ключ и возвращает 401, если он отсутствует или неверен.
     *
     * @param request HTTP-запрос
     * @param response HTTP-ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException ошибка сервлета
     * @throws IOException ошибка ввода-вывода
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String expected = appProperties.getSecurity().getApiKey();
        if (expected == null || expected.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }
        String provided = request.getHeader(API_KEY_HEADER);
        if (provided == null || !provided.equals(expected)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/plain;charset=UTF-8");
            response.getOutputStream().write("Unauthorized".getBytes(StandardCharsets.UTF_8));
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Пропускает проверку API-ключа для технических эндпоинтов и OPTIONS-запросов.
     *
     * @param request HTTP-запрос
     * @return true, если фильтр нужно пропустить
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return path != null && (path.startsWith("/actuator") || path.startsWith("/error"));
    }
}
