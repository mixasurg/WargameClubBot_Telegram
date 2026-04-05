package com.wargameclub.clubapi.security;

import java.io.IOException;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wargameclub.clubapi.config.AppProperties;
import com.wargameclub.clubapi.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Spring Security (JWT-аутентификация, RBAC, rate limit и аудит).
 */
@Configuration
public class SecurityConfig {

    /**
     * Сериализатор JSON для ответов security-ошибок.
     */
    private final ObjectMapper objectMapper;

    /**
     * Создает конфигурацию безопасности.
     *
     * @param objectMapper сериализатор JSON
     */
    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Создает PasswordEncoder для хеширования паролей пользователей.
     *
     * @return BCrypt-кодировщик
     */
    @org.springframework.context.annotation.Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Создает security filter chain приложения.
     *
     * @param http HttpSecurity-конфигуратор
     * @param appProperties настройки приложения
     * @param jwtService JWT-сервис
     * @param auditLogService сервис аудита
     * @return filter chain
     * @throws Exception ошибка конфигурирования
     */
    @org.springframework.context.annotation.Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AppProperties appProperties,
            JwtService jwtService,
            AuditLogService auditLogService
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        http.sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (!appProperties.getSecurity().isEnabled()) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);
        RateLimitFilter rateLimitFilter = new RateLimitFilter(appProperties);
        AuditLoggingFilter auditLoggingFilter = new AuditLoggingFilter(auditLogService);

        http.exceptionHandling(configurer -> configurer
                .authenticationEntryPoint((request, response, authException) -> writeError(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Требуется авторизация",
                        request
                ))
                .accessDeniedHandler((request, response, accessDeniedException) -> writeError(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Недостаточно прав",
                        request
                ))
        );

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/error", "/favicon.ico").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                .requestMatchers("/api/notifications/**").hasAnyRole("BOT_SERVICE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/events").hasAnyRole("ADMIN", "ORGANIZER", "BOT_SERVICE")
                .requestMatchers(HttpMethod.PUT, "/api/events/**").hasAnyRole("ADMIN", "ORGANIZER", "BOT_SERVICE")
                .requestMatchers(HttpMethod.POST, "/api/games").hasAnyRole("ADMIN", "ORGANIZER", "BOT_SERVICE")
                .requestMatchers("/api/telegram/settings/**").hasAnyRole("ADMIN", "BOT_SERVICE")
                .requestMatchers(HttpMethod.POST, "/api/users/register").hasAnyRole("ADMIN", "BOT_SERVICE")
                .requestMatchers(HttpMethod.POST, "/api/users/telegram").hasAnyRole("ADMIN", "BOT_SERVICE")
                .requestMatchers("/api/**").hasAnyRole("MEMBER", "ORGANIZER", "ADMIN", "BOT_SERVICE")
                .anyRequest().permitAll()
        );

        http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(auditLoggingFilter, AuthorizationFilter.class);

        return http.build();
    }

    /**
     * Формирует JSON-ответ с ошибкой security.
     *
     * @param response HTTP-ответ
     * @param status HTTP-статус
     * @param message описание ошибки
     * @param request HTTP-запрос
     * @throws IOException ошибка записи ответа
     */
    private void writeError(
            HttpServletResponse response,
            int status,
            String message,
            HttpServletRequest request
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorBody body = new ErrorBody(
                OffsetDateTime.now().toString(),
                status,
                message,
                request.getRequestURI()
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    /**
     * JSON-модель security-ошибки.
     *
     * @param timestamp время ошибки
     * @param status HTTP-статус
     * @param message описание ошибки
     * @param path путь запроса
     */
    private record ErrorBody(
            String timestamp,
            int status,
            String message,
            String path
    ) {
    }
}
