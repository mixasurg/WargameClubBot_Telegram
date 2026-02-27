package com.wargameclub.clubapi.service;

import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.exception.NotFoundException;
import com.wargameclub.clubapi.messaging.KafkaEventPublisher;
import com.wargameclub.clubapi.messaging.UserRegisteredEvent;
import com.wargameclub.clubapi.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис управления пользователями.
 */
@Service
public class UserService {

    /**
     * Репозиторий пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Публикатор событий в Kafka.
     */
    private final KafkaEventPublisher kafkaEventPublisher;

    /**
     * Создает сервис пользователей.
     *
     * @param userRepository репозиторий пользователей
     * @param kafkaEventPublisher публикатор Kafka-событий
     */
    public UserService(UserRepository userRepository, KafkaEventPublisher kafkaEventPublisher) {
        this.userRepository = userRepository;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    /**
     * Регистрирует нового пользователя и публикует событие регистрации.
     *
     * @param name имя пользователя
     * @return зарегистрированный пользователь
     */
    @Transactional
    public User register(String name) {
        User user = new User(name);
        User saved = userRepository.save(user);
        kafkaEventPublisher.publishUserRegistered(new UserRegisteredEvent(
                saved.getId(),
                saved.getName(),
                OffsetDateTime.now()
        ));
        return saved;
    }

    /**
     * Создает или обновляет пользователя по Telegram ID.
     *
     * @param telegramId идентификатор пользователя в Telegram
     * @param name имя пользователя
     * @return сохраненный пользователь
     */
    @Transactional
    public User upsertTelegramUser(Long telegramId, String name) {
        return userRepository.findByTelegramId(telegramId)
                .map(existing -> {
                    if (!existing.getName().equals(name)) {
                        existing.setName(name);
                    }
                    return existing;
                })
                .orElseGet(() -> userRepository.save(new User(name, telegramId)));
    }

    /**
     * Возвращает список пользователей по поисковому запросу.
     *
     * @param query строка поиска по имени
     * @return список пользователей
     */
    @Transactional(readOnly = true)
    public List<User> searchByName(String query) {
        if (query == null || query.isBlank()) {
            return userRepository.findAll();
        }
        return userRepository.findByNameContainingIgnoreCase(query);
    }

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return пользователь
     */
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }
}
