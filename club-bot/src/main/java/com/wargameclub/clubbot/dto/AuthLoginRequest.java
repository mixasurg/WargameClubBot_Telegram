package com.wargameclub.clubbot.dto;

/**
 * Запрос входа в club-api для сервисного пользователя.
 *
 * @param login логин пользователя
 * @param password пароль пользователя
 */
public record AuthLoginRequest(String login, String password) {
}
