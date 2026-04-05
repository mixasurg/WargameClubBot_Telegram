package com.wargameclub.clubapi.security;

import java.io.IOException;
import com.wargameclub.clubapi.service.AuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Фильтр аудита HTTP-запросов API.
 */
public class AuditLoggingFilter extends OncePerRequestFilter {

    /**
     * Логгер.
     */
    private static final Logger log = LoggerFactory.getLogger(AuditLoggingFilter.class);

    /**
     * Сервис записи аудита.
     */
    private final AuditLogService auditLogService;

    /**
     * Создает фильтр аудита.
     *
     * @param auditLogService сервис аудита
     */
    public AuditLoggingFilter(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Выполняет запрос и сохраняет запись аудита с результатом обработки.
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
        StatusCaptureResponseWrapper responseWrapper = new StatusCaptureResponseWrapper(response);
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, responseWrapper);
        } catch (Exception ex) {
            responseWrapper.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw ex;
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            try {
                AuthData authData = resolveActor();
                int status = responseWrapper.getStatus();
                auditLogService.log(
                        authData.userId(),
                        authData.login(),
                        authData.role(),
                        resolveAction(status),
                        request.getMethod(),
                        request.getRequestURI(),
                        request.getQueryString(),
                        status,
                        resolveClientIp(request),
                        request.getHeader("User-Agent"),
                        "durationMs=" + durationMs
                );
            } catch (Exception ex) {
                log.warn("Не удалось сохранить audit-запись", ex);
            }
        }
    }

    /**
     * Пропускает не-API запросы.
     *
     * @param request HTTP-запрос
     * @return true, если фильтр нужно пропустить
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/");
    }

    /**
     * Возвращает действие аудита в зависимости от результата запроса.
     *
     * @param status HTTP-статус ответа
     * @return код действия аудита
     */
    private String resolveAction(int status) {
        return status >= 400 ? "HTTP_REQUEST_FAILED" : "HTTP_REQUEST";
    }

    /**
     * Возвращает данные авторизованного пользователя из SecurityContext.
     *
     * @return данные пользователя или null-поля, если пользователь не аутентифицирован
     */
    private AuthData resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthData(null, null, null);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUserPrincipal userPrincipal) {
            return new AuthData(userPrincipal.getUserId(), userPrincipal.getLogin(), userPrincipal.getRole());
        }
        String login = principal instanceof String ? (String) principal : authentication.getName();
        if ("anonymousUser".equals(login)) {
            login = null;
        }
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority != null && authority.startsWith("ROLE_"))
                .findFirst()
                .map(authority -> authority.substring("ROLE_".length()))
                .orElse(null);
        return new AuthData(null, login, role);
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
     * Набор данных об акторе аудита.
     *
     * @param userId идентификатор пользователя
     * @param login логин пользователя
     * @param role роль пользователя
     */
    private record AuthData(Long userId, String login, String role) {
    }

    /**
     * Обертка HttpServletResponse, сохраняющая последний установленный статус.
     */
    private static class StatusCaptureResponseWrapper extends HttpServletResponseWrapper {

        /**
         * Последний установленный HTTP-статус.
         */
        private int status = SC_OK;

        /**
         * Создает обертку ответа.
         *
         * @param response исходный HTTP-ответ
         */
        StatusCaptureResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        /**
         * Устанавливает HTTP-статус и сохраняет его значение.
         *
         * @param sc HTTP-статус
         */
        @Override
        public void setStatus(int sc) {
            this.status = sc;
            super.setStatus(sc);
        }

        /**
         * Отправляет ошибку и сохраняет HTTP-статус.
         *
         * @param sc HTTP-статус
         * @throws IOException ошибка ввода-вывода
         */
        @Override
        public void sendError(int sc) throws IOException {
            this.status = sc;
            super.sendError(sc);
        }

        /**
         * Отправляет ошибку и сохраняет HTTP-статус.
         *
         * @param sc HTTP-статус
         * @param msg сообщение ошибки
         * @throws IOException ошибка ввода-вывода
         */
        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.status = sc;
            super.sendError(sc, msg);
        }

        /**
         * Возвращает последний установленный HTTP-статус.
         *
         * @return HTTP-статус
         */
        @Override
        public int getStatus() {
            return status;
        }
    }
}
