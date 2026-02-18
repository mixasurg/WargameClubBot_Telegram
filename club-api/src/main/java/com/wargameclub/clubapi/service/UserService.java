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

@Service
public class UserService {
    private final UserRepository userRepository;
    private final KafkaEventPublisher kafkaEventPublisher;

    public UserService(UserRepository userRepository, KafkaEventPublisher kafkaEventPublisher) {
        this.userRepository = userRepository;
        this.kafkaEventPublisher = kafkaEventPublisher;
    }

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

    @Transactional(readOnly = true)
    public List<User> searchByName(String query) {
        if (query == null || query.isBlank()) {
            return userRepository.findAll();
        }
        return userRepository.findByNameContainingIgnoreCase(query);
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }
}

