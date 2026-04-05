package com.wargameclub.clubapi.security;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import com.wargameclub.clubapi.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Фильтр ограничения частоты HTTP-запросов.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    /**
     * Хранилище счетчиков запросов по ключу клиента.
     */
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    /**
     * Счетчик обработанных запросов для периодической очистки старых бакетов.
     */
    private final AtomicLong requests = new AtomicLong(0);

    /**
     * Настройки приложения.
     */
    private final AppProperties appProperties;

    /**
     * Создает фильтр rate limit.
     *
     * @param appProperties настройки приложения
     */
    public RateLimitFilter(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * Применяет ограничение частоты запросов к API-эндпоинтам.
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
        if (!appProperties.getSecurity().isRateLimitEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        long currentMinute = System.currentTimeMillis() / 60_000L;
        boolean loginEndpoint = isLoginEndpoint(request);
        int limit = loginEndpoint
                ? appProperties.getSecurity().getLoginRateLimitPerMinute()
                : appProperties.getSecurity().getRateLimitPerMinute();

        String clientIp = resolveClientIp(request);
        String bucket = loginEndpoint ? "LOGIN" : "API";
        String key = bucket + "|" + clientIp;
        Counter counter = counters.compute(key, (k, previous) -> {
            if (previous == null || previous.minute() != currentMinute) {
                return new Counter(currentMinute, 1);
            }
            return new Counter(previous.minute(), previous.count() + 1);
        });

        if (counter.count() > limit) {
            rejectTooManyRequests(response);
            return;
        }

        cleanupOldBucketsIfNeeded(currentMinute);
        filterChain.doFilter(request, response);
    }

    /**
     * Пропускает rate limit для служебных/не-API эндпоинтов.
     *
     * @param request HTTP-запрос
     * @return true, если фильтр нужно пропустить
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return true;
        }
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/error")
                || !path.startsWith("/api/");
    }

    /**
     * Возвращает true для POST /api/auth/login.
     *
     * @param request HTTP-запрос
     * @return true, если это login endpoint
     */
    private boolean isLoginEndpoint(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && "/api/auth/login".equals(request.getRequestURI());
    }

    /**
     * Формирует ответ 429 Too Many Requests.
     *
     * @param response HTTP-ответ
     * @throws IOException ошибка записи ответа
     */
    private void rejectTooManyRequests(HttpServletResponse response) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        long secondsUntilNextMinute = 60 - ((System.currentTimeMillis() / 1000) % 60);
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(secondsUntilNextMinute));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":429,\"message\":\"Слишком много запросов\"}");
    }

    /**
     * Удаляет устаревшие бакеты из памяти.
     *
     * @param currentMinute текущая минута
     */
    private void cleanupOldBucketsIfNeeded(long currentMinute) {
        long currentRequests = requests.incrementAndGet();
        if (currentRequests % 200 != 0) {
            return;
        }
        long staleBefore = currentMinute - 1;
        counters.entrySet().removeIf(entry -> entry.getValue().minute() < staleBefore);
    }

    /**
     * Возвращает клиентский IP из X-Forwarded-For или remote address.
     *
     * @param request HTTP-запрос
     * @return IP клиента
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma >= 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Счетчик запросов в минутном бакете.
     *
     * @param minute номер минуты (epoch-minute)
     * @param count число запросов в текущем бакете
     */
    private record Counter(long minute, int count) {
    }
}
