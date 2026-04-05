package com.wargameclub.clubapi.security;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import com.wargameclub.clubapi.config.AppProperties;
import com.wargameclub.clubapi.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/**
 * Сервис генерации и валидации JWT-токенов.
 */
@Service
public class JwtService {

    /**
     * Настройки приложения.
     */
    private final AppProperties appProperties;

    /**
     * Создает JWT-сервис.
     *
     * @param appProperties настройки приложения
     */
    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * Генерирует JWT-токен для пользователя.
     *
     * @param user пользователь
     * @return результат генерации токена
     */
    public TokenResult generate(User user) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = now.plusSeconds(appProperties.getSecurity().getJwtTtlSeconds());
        String token = Jwts.builder()
                .subject(safe(user.getLogin()))
                .claim("uid", user.getId())
                .claim("role", user.getRole() == null ? "MEMBER" : user.getRole().name())
                .claim("name", safe(user.getName()))
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(expiresAt.toInstant()))
                .signWith(secretKey())
                .compact();
        return new TokenResult(token, expiresAt);
    }

    /**
     * Валидирует токен и извлекает principal пользователя.
     *
     * @param token JWT-токен
     * @return principal пользователя
     */
    public AuthenticatedUserPrincipal parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Object userIdClaim = claims.get("uid");
        Long userId = userIdClaim instanceof Number number ? number.longValue() : null;
        String login = claims.getSubject();
        String role = claims.get("role", String.class);
        String name = claims.get("name", String.class);
        return new AuthenticatedUserPrincipal(userId, safe(login), safe(role), safe(name));
    }

    /**
     * Возвращает ключ подписи JWT.
     *
     * @return ключ подписи
     */
    private SecretKey secretKey() {
        byte[] bytes = appProperties.getSecurity().getJwtSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }

    /**
     * Возвращает безопасную строку без null.
     *
     * @param value значение
     * @return строка без null
     */
    private String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * Результат генерации JWT.
     *
     * @param token токен
     * @param expiresAt время истечения
     */
    public record TokenResult(String token, OffsetDateTime expiresAt) {
    }
}
