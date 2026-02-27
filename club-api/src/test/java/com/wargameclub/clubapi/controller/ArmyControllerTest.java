package com.wargameclub.clubapi.controller;

import java.time.OffsetDateTime;
import java.util.List;
import com.wargameclub.clubapi.dto.ArmyCreateRequest;
import com.wargameclub.clubapi.dto.ArmyDto;
import com.wargameclub.clubapi.dto.ArmyUsageRequest;
import com.wargameclub.clubapi.entity.Army;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.service.ArmyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArmyControllerTest {

    @Mock
    private ArmyService armyService;

    private ArmyController controller;

    @BeforeEach
    void setUp() {
        controller = new ArmyController(armyService);
    }

    @Test
    void listDecodesQueryParams() {
        when(armyService.find(eq("Warhammer 40K"), eq("Blood Angels"), eq(true), eq(1L), eq(true)))
                .thenReturn(List.of());

        controller.list("Warhammer%2040K", "Blood%20Angels", true, 1L, true);

        verify(armyService).find("Warhammer 40K", "Blood Angels", true, 1L, true);
    }

    @Test
    void createReturnsMappedDto() {
        User owner = new User("Owner");
        ReflectionTestUtils.setField(owner, "id", 10L);
        Army army = new Army(owner, "Game", "Faction", true);
        ReflectionTestUtils.setField(army, "id", 5L);
        ReflectionTestUtils.setField(army, "createdAt", OffsetDateTime.parse("2026-01-01T10:00:00+03:00"));

        when(armyService.create(10L, "Game", "Faction", true)).thenReturn(army);

        ArmyDto dto = controller.create(new ArmyCreateRequest(10L, "Game", "Faction", true));

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.ownerUserId()).isEqualTo(10L);
        assertThat(dto.game()).isEqualTo("Game");
        assertThat(dto.isClubShared()).isTrue();
    }

    @Test
    void useDelegatesToService() {
        OffsetDateTime usedAt = OffsetDateTime.now();
        controller.use(7L, new ArmyUsageRequest(2L, usedAt, "note"));

        verify(armyService).useArmy(7L, 2L, usedAt, "note");
    }
}
