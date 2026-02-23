package com.wargameclub.clubapi.repository;

import com.wargameclub.clubapi.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для пользователя.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Возвращает пользователя.
     */
    Optional<User> findByTelegramId(Long telegramId);

    /**
     * Возвращает пользователя.
     */
    List<User> findByNameContainingIgnoreCase(String name);
}

