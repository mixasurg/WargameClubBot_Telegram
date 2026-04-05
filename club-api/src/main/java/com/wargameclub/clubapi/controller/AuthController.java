package com.wargameclub.clubapi.controller;

import com.wargameclub.clubapi.dto.AuthLoginRequest;
import com.wargameclub.clubapi.dto.AuthMeResponse;
import com.wargameclub.clubapi.dto.AuthRegisterRequest;
import com.wargameclub.clubapi.dto.AuthTokenResponse;
import com.wargameclub.clubapi.security.AuthService;
import com.wargameclub.clubapi.security.AuthenticatedUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST-контроллер аутентификации пользователей (JWT).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Сервис аутентификации.
     */
    private final AuthService authService;

    /**
     * Создает auth-контроллер.
     *
     * @param authService сервис аутентификации
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Регистрирует нового пользователя и выдает JWT-токен.
     *
     * @param request данные регистрации
     * @param httpRequest HTTP-запрос
     * @return токен зарегистрированного пользователя
     */
    @PostMapping("/register")
    public AuthTokenResponse register(
            @Valid @RequestBody AuthRegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        return authService.register(
                request,
                resolveClientIp(httpRequest),
                httpRequest.getHeader("User-Agent")
        );
    }

    /**
     * Выполняет вход по логину и паролю и выдает JWT-токен.
     *
     * @param request данные входа
     * @param httpRequest HTTP-запрос
     * @return токен пользователя
     */
    @PostMapping("/login")
    public AuthTokenResponse login(
            @Valid @RequestBody AuthLoginRequest request,
            HttpServletRequest httpRequest
    ) {
        return authService.login(
                request,
                resolveClientIp(httpRequest),
                httpRequest.getHeader("User-Agent")
        );
    }

    /**
     * Возвращает информацию о текущем аутентифицированном пользователе.
     *
     * @param authentication данные аутентификации
     * @return профиль текущего пользователя
     */
    @GetMapping("/me")
    public AuthMeResponse me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Не авторизован");
        }
        return authService.me(principal);
    }

    /**
     * Возвращает IP клиента из X-Forwarded-For или remote address.
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
}
