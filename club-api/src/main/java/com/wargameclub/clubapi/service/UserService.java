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
 * Сервис для работы с пользователями.
 */
@Service
public class UserService {

    /**
     * Репозиторий пользователя.
     */
    private final UserRepository userRepository;

    /**
     * Поле состояния.
     */
    private final KafkaEventPublisher kafkaEventPublisher;

    /**
     * Конструктор UserService.
     */
    public UserService(UserRepository userRepository, KafkaEventPublisher kafkaEventPublisher) {
        this.userRepository = userRepository;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

    /**
     * Регистрирует пользователя.
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
     * Создает или обновляет TelegramUser.
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
     * Возвращает список пользователей.
     */
    @Transactional(readOnly = true)
    public List<User> searchByName(String query) {
        if (query == null || query.isBlank()) {
            return userRepository.findAll();
        }
        return userRepository.findByNameContainingIgnoreCase(query);
    }

    /**
     * Возвращает пользователя.
     */
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }
}

