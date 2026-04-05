package com.wargameclub.clubapi.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wargameclub.clubapi.dto.AuthRegisterRequest;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.enums.UserRole;
import com.wargameclub.clubapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты JWT-аутентификации и RBAC-ограничений.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.security.enabled=true",
        "app.security.rate-limit-enabled=false",
        "app.security.jwt-secret=test-secret-test-secret-test-secret-12345"
})
class AuthSecurityIntegrationTest {

    /**
     * MockMvc.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * ObjectMapper.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Репозиторий пользователей.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * PasswordEncoder.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Подготовка сервисного BOT-пользователя.
     */
    @BeforeEach
    void setUp() {
        if (userRepository.findByLoginIgnoreCase("botsvc").isPresent()) {
            return;
        }
        User bot = new User("Bot Service");
        bot.setLogin("botsvc");
        bot.setPasswordHash(passwordEncoder.encode("bot-pass-123"));
        bot.setRole(UserRole.BOT_SERVICE);
        bot.setEnabled(true);
        userRepository.save(bot);
    }

    /**
     * Проверяет сценарий регистрации и доступ к профилю /api/auth/me по выданному JWT.
     *
     * @throws Exception ошибка теста
     */
    @Test
    void registerAndAccessMe() throws Exception {
        String login = "member_lab4";
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthRegisterRequest("Member Lab4", login, "strongpass123")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andReturn();

        String token = extractToken(registerResult);
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(login))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    /**
     * Проверяет, что защищенные API-эндпоинты недоступны без JWT.
     *
     * @throws Exception ошибка теста
     */
    @Test
    void rejectsProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Проверяет RBAC: MEMBER не может читать notification outbox, BOT_SERVICE может.
     *
     * @throws Exception ошибка теста
     */
    @Test
    void rbacForNotifications() throws Exception {
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AuthRegisterRequest("Regular Member", "member_rbac", "strongpass123")
                        )))
                .andExpect(status().isOk())
                .andReturn();
        String memberToken = extractToken(registerResult);

        mockMvc.perform(get("/api/notifications/pending")
                        .queryParam("target", "TELEGRAM")
                        .queryParam("limit", "5")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());

        String botToken = loginAndExtractToken("botsvc", "bot-pass-123");
        mockMvc.perform(get("/api/notifications/pending")
                        .queryParam("target", "TELEGRAM")
                        .queryParam("limit", "5")
                        .header("Authorization", "Bearer " + botToken))
                .andExpect(status().isOk());
    }

    /**
     * Выполняет login и возвращает токен.
     *
     * @param login логин
     * @param password пароль
     * @return JWT-токен
     * @throws Exception ошибка теста
     */
    private String loginAndExtractToken(String login, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"" + login + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return extractToken(loginResult);
    }

    /**
     * Извлекает accessToken из JSON-ответа.
     *
     * @param result результат MockMvc
     * @return JWT access token
     * @throws Exception ошибка парсинга
     */
    private String extractToken(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.path("accessToken").asText();
    }
}
