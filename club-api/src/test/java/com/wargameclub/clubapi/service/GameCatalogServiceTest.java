package com.wargameclub.clubapi.service;

import java.util.List;
import java.util.Optional;
import com.wargameclub.clubapi.entity.GameCatalog;
import com.wargameclub.clubapi.exception.BadRequestException;
import com.wargameclub.clubapi.repository.GameCatalogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameCatalogServiceTest {

    @Mock
    private GameCatalogRepository repository;

    private GameCatalogService service;

    @BeforeEach
    void setUp() {
        service = new GameCatalogService(repository);
    }

    @Test
    void listReturnsAllWhenNoFilter() {
        when(repository.findAll()).thenReturn(List.of());

        List<GameCatalog> result = service.list(null);

        assertThat(result).isEmpty();
        verify(repository).findAll();
    }

    @Test
    void createOrGetRejectsBlankName() {
        assertThatThrownBy(() -> service.createOrGet(" ", 120, 2))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createOrGetRejectsTooLongName() {
        String name = "A".repeat(121);
        assertThatThrownBy(() -> service.createOrGet(name, 120, 2))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createOrGetReturnsExisting() {
        GameCatalog existing = new GameCatalog("Game", 120, 2);
        when(repository.findFirstByNameIgnoreCase("Game")).thenReturn(Optional.of(existing));

        GameCatalog result = service.createOrGet("Game", 120, 2);

        assertThat(result).isSameAs(existing);
    }

    @Test
    void createOrGetCreatesNew() {
        when(repository.findFirstByNameIgnoreCase("Game")).thenReturn(Optional.empty());
        when(repository.save(any(GameCatalog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameCatalog result = service.createOrGet(" Game ", 90, 1);

        ArgumentCaptor<GameCatalog> captor = ArgumentCaptor.forClass(GameCatalog.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Game");
        assertThat(result.getName()).isEqualTo("Game");
    }
}
