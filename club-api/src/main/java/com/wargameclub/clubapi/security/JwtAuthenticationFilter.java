package com.wargameclub.clubapi.security;

import java.io.IOException;
import java.util.List;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Фильтр JWT-аутентификации: извлекает bearer-токен и заполняет SecurityContext.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Префикс заголовка Authorization для Bearer токена.
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Сервис валидации JWT.
     */
    private final JwtService jwtService;

    /**
     * Создает JWT-фильтр.
     *
     * @param jwtService сервис JWT
     */
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Проверяет Authorization-заголовок, валидирует JWT и устанавливает principal.
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
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            unauthorized(response, "Пустой bearer-токен");
            return;
        }

        try {
            AuthenticatedUserPrincipal principal = jwtService.parse(token);
            String role = normalizeRole(principal.getRole());
            UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                    principal,
                    token,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            unauthorized(response, "Недействительный JWT-токен");
        }
    }

    /**
     * Возвращает роль пользователя в верхнем регистре.
     *
     * @param role роль из JWT
     * @return нормализованная роль
     */
    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "MEMBER";
        }
        return role.trim().toUpperCase(java.util.Locale.ROOT);
    }

    /**
     * Формирует ответ 401 с JSON-ошибкой.
     *
     * @param response HTTP-ответ
     * @param message текст ошибки
     * @throws IOException ошибка записи ответа
     */
    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":401,\"message\":\"" + message + "\"}");
    }
}
