package com.wargameclub.clubapi.repository;

import com.wargameclub.clubapi.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для пользователей.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Ищет пользователя по Telegram ID.
     *
     * @param telegramId идентификатор пользователя в Telegram
     * @return пользователь, если найден
     */
    Optional<User> findByTelegramId(Long telegramId);

    /**
     * Ищет пользователя по логину без учета регистра.
     *
     * @param login логин пользователя
     * @return пользователь, если найден
     */
    Optional<User> findByLoginIgnoreCase(String login);

    /**
     * Ищет пользователей по части имени без учета регистра.
     *
     * @param name часть имени
     * @return список пользователей
     */
    List<User> findByNameContainingIgnoreCase(String name);
}
