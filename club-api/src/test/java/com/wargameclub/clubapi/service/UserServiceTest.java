package com.wargameclub.clubapi.service;

import java.util.List;
import java.util.Optional;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.exception.NotFoundException;
import com.wargameclub.clubapi.messaging.KafkaEventPublisher;
import com.wargameclub.clubapi.messaging.UserRegisteredEvent;
import com.wargameclub.clubapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, kafkaEventPublisher);
    }

    @Test
    void registerPublishesEvent() {
        User saved = new User("Alice");
        ReflectionTestUtils.setField(saved, "id", 1L);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = service.register("Alice");

        assertThat(result).isSameAs(saved);
        ArgumentCaptor<UserRegisteredEvent> captor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(kafkaEventPublisher).publishUserRegistered(captor.capture());
        assertThat(captor.getValue().userId()).isEqualTo(1L);
    }

    @Test
    void upsertTelegramUserUpdatesExisting() {
        User existing = new User("Old", 100L);
        when(userRepository.findByTelegramId(100L)).thenReturn(Optional.of(existing));

        User result = service.upsertTelegramUser(100L, "New");

        assertThat(result.getName()).isEqualTo("New");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void upsertTelegramUserCreatesNew() {
        when(userRepository.findByTelegramId(200L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.upsertTelegramUser(200L, "User");

        assertThat(result.getTelegramId()).isEqualTo(200L);
        assertThat(result.getName()).isEqualTo("User");
    }

    @Test
    void searchByNameReturnsAllWhenBlank() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> result = service.searchByName(" ");

        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    void searchByNameDelegatesToRepository() {
        when(userRepository.findByNameContainingIgnoreCase("al")).thenReturn(List.of(new User("Alice")));

        List<User> result = service.searchByName("al");

        assertThat(result).hasSize(1);
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(10L))
                .isInstanceOf(NotFoundException.class);
    }
}
