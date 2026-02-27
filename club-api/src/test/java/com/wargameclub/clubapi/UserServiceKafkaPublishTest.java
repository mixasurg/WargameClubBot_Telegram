package com.wargameclub.clubapi;

import com.wargameclub.clubapi.messaging.KafkaEventPublisher;
import com.wargameclub.clubapi.messaging.UserRegisteredEvent;
import com.wargameclub.clubapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

/**
 * Интеграционные тесты публикации события регистрации пользователя.
 */
@SpringBootTest
@ActiveProfiles("test")
public class UserServiceKafkaPublishTest {

    /**
     * Сервис пользователей.
     */
    @Autowired
    private UserService userService;

    /**
     * Публикатор Kafka-событий (мок).
     */
    @MockBean
    private KafkaEventPublisher kafkaEventPublisher;

    /**
     * Проверяет, что при регистрации публикуется событие UserRegistered.
     */
    @Test
    void registerPublishesUserRegistered() {
        userService.register("Alice");

        ArgumentCaptor<UserRegisteredEvent> captor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(kafkaEventPublisher).publishUserRegistered(captor.capture());
        UserRegisteredEvent payload = captor.getValue();

        assertEquals("Alice", payload.name());
    }
}
