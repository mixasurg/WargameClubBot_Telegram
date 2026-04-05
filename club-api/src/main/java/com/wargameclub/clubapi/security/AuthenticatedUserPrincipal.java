package com.wargameclub.clubapi.security;

import java.security.Principal;

/**
 * Principal аутентифицированного пользователя из JWT.
 */
public class AuthenticatedUserPrincipal implements Principal {

    /**
     * Идентификатор пользователя.
     */
    private final Long userId;

    /**
     * Логин пользователя.
     */
    private final String login;

    /**
     * Роль пользователя.
     */
    private final String role;

    /**
     * Отображаемое имя пользователя.
     */
    private final String name;

    /**
     * Создает principal аутентифицированного пользователя.
     *
     * @param userId идентификатор пользователя
     * @param login логин пользователя
     * @param role роль пользователя
     * @param name отображаемое имя пользователя
     */
    public AuthenticatedUserPrincipal(Long userId, String login, String role, String name) {
        this.userId = userId;
        this.login = login;
        this.role = role;
        this.name = name;
    }

    /**
     * Возвращает идентификатор пользователя.
     *
     * @return идентификатор пользователя
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Возвращает логин пользователя.
     *
     * @return логин пользователя
     */
    public String getLogin() {
        return login;
    }

    /**
     * Возвращает роль пользователя.
     *
     * @return роль пользователя
     */
    public String getRole() {
        return role;
    }

    /**
     * Возвращает отображаемое имя пользователя.
     *
     * @return отображаемое имя пользователя
     */
    @Override
    public String getName() {
        return name;
    }
}
