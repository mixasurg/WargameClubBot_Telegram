package com.wargameclub.clubapi.controller;

import java.util.List;
import com.wargameclub.clubapi.dto.TelegramUserUpsertRequest;
import com.wargameclub.clubapi.dto.UserDto;
import com.wargameclub.clubapi.dto.UserGameStatsDto;
import com.wargameclub.clubapi.dto.UserRegisterRequest;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.entity.UserGameStats;
import com.wargameclub.clubapi.service.GameResultService;
import com.wargameclub.clubapi.service.LoyaltyService;
import com.wargameclub.clubapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private GameResultService resultService;
    @Mock
    private LoyaltyService loyaltyService;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(userService, resultService, loyaltyService);
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

    @Test
    void getPrivateStatsReturnsAggregatedDto() {
        when(resultService.getResultSnapshot(5L, null)).thenReturn(new GameResultService.ResultSnapshot(6, 2, 2));
        when(resultService.getResultSnapshot(org.mockito.ArgumentMatchers.eq(5L), notNull()))
                .thenReturn(new GameResultService.ResultSnapshot(2, 1, 1));
        when(loyaltyService.getPoints(5L)).thenReturn(120);

        var dto = controller.getPrivateStats(5L);

        assertThat(dto.userId()).isEqualTo(5L);
        assertThat(dto.loyaltyPoints()).isEqualTo(120);
        assertThat(dto.totalGames()).isEqualTo(10);
        assertThat(dto.gamesLastMonth()).isEqualTo(4);
        assertThat(dto.winRateTotal()).isEqualTo(60.0);
        assertThat(dto.winRateLastMonth()).isEqualTo(50.0);
        verify(resultService).getResultSnapshot(5L, null);
        verify(resultService).getResultSnapshot(org.mockito.ArgumentMatchers.eq(5L), notNull());
        verify(loyaltyService).getPoints(5L);
    }
}
