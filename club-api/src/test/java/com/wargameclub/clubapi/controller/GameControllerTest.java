package com.wargameclub.clubapi.controller;

import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubapi.dto.GameCreateRequest;
import com.wargameclub.clubapi.dto.GameDto;
import com.wargameclub.clubapi.entity.GameCatalog;
import com.wargameclub.clubapi.service.GameCatalogService;
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
class GameControllerTest {

    @Mock
    private GameCatalogService gameCatalogService;

    private GameController controller;

    @BeforeEach
    void setUp() {
        controller = new GameController(gameCatalogService);
    }

    @Test
    void listReturnsDtos() {
        GameCatalog game = new GameCatalog("Game", 120, 2);
        ReflectionTestUtils.setField(game, "id", 1L);
        ReflectionTestUtils.setField(game, "createdAt", OffsetDateTime.parse("2026-01-01T10:00:00+03:00"));
        when(gameCatalogService.list(null)).thenReturn(List.of(game));

        List<GameDto> result = controller.list(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Game");
    }

    @Test
    void createDelegatesToService() {
        GameCatalog game = new GameCatalog("Game", 90, 1);
        ReflectionTestUtils.setField(game, "id", 2L);
        when(gameCatalogService.createOrGet("Game", 90, 1)).thenReturn(game);

        GameDto dto = controller.create(new GameCreateRequest("Game", 90, 1));

        assertThat(dto.id()).isEqualTo(2L);
        verify(gameCatalogService).createOrGet("Game", 90, 1);
    }
}
