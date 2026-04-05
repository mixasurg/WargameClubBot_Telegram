package com.wargameclub.clubapi.security;

import java.time.OffsetDateTime;
import java.util.Locale;
import com.wargameclub.clubapi.dto.AuthLoginRequest;
import com.wargameclub.clubapi.dto.AuthMeResponse;
import com.wargameclub.clubapi.dto.AuthRegisterRequest;
import com.wargameclub.clubapi.dto.AuthTokenResponse;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.enums.UserRole;
import com.wargameclub.clubapi.exception.BadRequestException;
import com.wargameclub.clubapi.repository.UserRepository;
import com.wargameclub.clubapi.service.AuditLogService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Сервис аутентификации и регистрации пользователей.
 */
@Service
public class AuthService {

    /**
     * Репозиторий пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Кодировщик паролей.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Сервис JWT.
     */
    private final JwtService jwtService;

    /**
     * Сервис аудита.
     */
    private final AuditLogService auditLogService;

    /**
     * Создает сервис аутентификации.
     *
     * @param userRepository репозиторий пользователей
     * @param passwordEncoder кодировщик паролей
     * @param jwtService JWT-сервис
     * @param auditLogService сервис аудита
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
    }

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param request данные регистрации
     * @param clientIp IP клиента
     * @param userAgent User-Agent клиента
     * @return JWT-токен зарегистрированного пользователя
     */
    @Transactional
    public AuthTokenResponse register(AuthRegisterRequest request, String clientIp, String userAgent) {
        String login = normalizeLogin(request.login());
        if (userRepository.findByLoginIgnoreCase(login).isPresent()) {
            throw new BadRequestException("Логин уже занят");
        }
        User user = new User(request.name());
        user.setLogin(login);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.MEMBER);
        user.setEnabled(true);
        User saved = userRepository.save(user);

        auditLogService.log(
                saved.getId(),
                saved.getLogin(),
                saved.getRole().name(),
                "AUTH_REGISTER",
                "POST",
                "/api/auth/register",
                null,
                HttpStatus.OK.value(),
                clientIp,
                userAgent,
                "Успешная регистрация"
        );
        return buildTokenResponse(saved);
    }

    /**
     * Выполняет вход пользователя по логину и паролю.
     *
     * @param request данные входа
     * @param clientIp IP клиента
     * @param userAgent User-Agent клиента
     * @return JWT-токен пользователя
     */
    @Transactional
    public AuthTokenResponse login(AuthLoginRequest request, String clientIp, String userAgent) {
        String login = normalizeLogin(request.login());
        User user = userRepository.findByLoginIgnoreCase(login)
                .orElseThrow(() -> unauthorized(login, clientIp, userAgent, "Пользователь не найден"));

        if (!user.isEnabled()) {
            throw unauthorized(login, clientIp, userAgent, "Аккаунт отключен");
        }
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw unauthorized(login, clientIp, userAgent, "Неверный пароль");
        }

        user.setLastLoginAt(OffsetDateTime.now());
        auditLogService.log(
                user.getId(),
                user.getLogin(),
                user.getRole() == null ? null : user.getRole().name(),
                "AUTH_LOGIN_SUCCESS",
                "POST",
                "/api/auth/login",
                null,
                HttpStatus.OK.value(),
                clientIp,
                userAgent,
                "Успешный вход"
        );
        return buildTokenResponse(user);
    }

    /**
     * Возвращает информацию о текущем аутентифицированном пользователе.
     *
     * @param principal principal пользователя
     * @return информация о пользователе
     */
    @Transactional(readOnly = true)
    public AuthMeResponse me(AuthenticatedUserPrincipal principal) {
        return new AuthMeResponse(
                principal.getUserId(),
                principal.getLogin(),
                principal.getRole(),
                principal.getName()
        );
    }

    /**
     * Формирует response с JWT-токеном.
     *
     * @param user пользователь
     * @return response с токеном
     */
    private AuthTokenResponse buildTokenResponse(User user) {
        JwtService.TokenResult tokenResult = jwtService.generate(user);
        return new AuthTokenResponse(
                tokenResult.token(),
                "Bearer",
                tokenResult.expiresAt(),
                user.getId(),
                user.getLogin(),
                user.getRole() == null ? null : user.getRole().name(),
                user.getName()
        );
    }

    /**
     * Формирует exception 401 и пишет аудит неуспешного входа.
     *
     * @param login логин
     * @param clientIp IP клиента
     * @param userAgent User-Agent клиента
     * @param details детали ошибки
     * @return exception 401
     */
    private ResponseStatusException unauthorized(String login, String clientIp, String userAgent, String details) {
        auditLogService.log(
                null,
                login,
                null,
                "AUTH_LOGIN_FAILED",
                "POST",
                "/api/auth/login",
                null,
                HttpStatus.UNAUTHORIZED.value(),
                clientIp,
                userAgent,
                details
        );
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
    }

    /**
     * Нормализует логин (trim + lowercase).
     *
     * @param login логин
     * @return нормализованный логин
     */
    private String normalizeLogin(String login) {
        return login == null ? null : login.trim().toLowerCase(Locale.ROOT);
    }
}
