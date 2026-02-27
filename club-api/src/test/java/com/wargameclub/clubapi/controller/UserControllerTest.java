package com.wargameclub.clubapi.controller;

import java.util.List;
import com.wargameclub.clubapi.dto.TelegramUserUpsertRequest;
import com.wargameclub.clubapi.dto.UserDto;
import com.wargameclub.clubapi.dto.UserGameStatsDto;
import com.wargameclub.clubapi.dto.UserRegisterRequest;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.entity.UserGameStats;
import com.wargameclub.clubapi.service.GameResultService;
import com.wargameclub.clubapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private GameResultService resultService;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(userService, resultService);
    }

    @Test
    void registerReturnsDto() {
        User user = new User("Alice");
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userService.register("Alice")).thenReturn(user);

        UserDto dto = controller.register(new UserRegisterRequest("Alice"));

        assertThat(dto.id()).isEqualTo(1L);
        verify(userService).register("Alice");
    }

    @Test
    void upsertTelegramReturnsDto() {
        User user = new User("Bob", 100L);
        ReflectionTestUtils.setField(user, "id", 2L);
        when(userService.upsertTelegramUser(100L, "Bob")).thenReturn(user);

        UserDto dto = controller.upsertTelegram(new TelegramUserUpsertRequest(100L, "Bob"));

        assertThat(dto.telegramId()).isEqualTo(100L);
    }

    @Test
    void listReturnsDtos() {
        User user = new User("Alice");
        ReflectionTestUtils.setField(user, "id", 3L);
        when(userService.searchByName("Al")).thenReturn(List.of(user));

        List<UserDto> result = controller.list("Al");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(3L);
    }

    @Test
    void getStatsReturnsDto() {
        User user = new User("Alice");
        ReflectionTestUtils.setField(user, "id", 4L);
        UserGameStats stats = new UserGameStats(user);
        when(resultService.getStats(4L)).thenReturn(stats);

        UserGameStatsDto dto = controller.getStats(4L);

        assertThat(dto.userId()).isEqualTo(4L);
    }
}
