package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import com.wargameclub.clubapi.entity.Army;
import com.wargameclub.clubapi.entity.ArmyUsage;
import com.wargameclub.clubapi.entity.User;
import com.wargameclub.clubapi.exception.BadRequestException;
import com.wargameclub.clubapi.exception.NotFoundException;
import com.wargameclub.clubapi.repository.ArmyRepository;
import com.wargameclub.clubapi.repository.ArmyUsageRepository;
import com.wargameclub.clubapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArmyServiceTest {

    @Mock
    private ArmyRepository armyRepository;
    @Mock
    private ArmyUsageRepository usageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LoyaltyService loyaltyService;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ArmyService armyService;

    @Test
    void createClubSharedAddsPoints() {
        User owner = new User("Owner");
        ReflectionTestUtils.setField(owner, "id", 10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(armyRepository.save(any(Army.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Army result = armyService.create(10L, "Game", "Faction", true);

        assertThat(result.getOwner()).isSameAs(owner);
        verify(loyaltyService).addPointsForSharedArmy(10L);
    }

    @Test
    void createThrowsWhenUserMissing() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> armyService.create(5L, "Game", "Faction", false))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findDelegatesToRepository() {
        when(armyRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<Army> result = armyService.find("Game", "Faction", true, 1L, true);

        assertThat(result).isEmpty();
        verify(armyRepository).findAll(any(Specification.class));
    }

    @Test
    void deactivateMarksArmyInactive() {
        Army army = new Army(new User("Owner"), "Game", "Faction", false);
        ReflectionTestUtils.setField(army, "id", 2L);
        when(armyRepository.findById(2L)).thenReturn(Optional.of(army));

        Army result = armyService.deactivate(2L);

        assertThat(result.isActive()).isFalse();
    }

    @Test
    void useArmyRequiresUsedAt() {
        assertThatThrownBy(() -> armyService.useArmy(1L, 2L, null, "notes"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void useArmyRejectsInactiveArmy() {
        Army army = new Army(new User("Owner"), "Game", "Faction", false);
        ReflectionTestUtils.setField(army, "id", 3L);
        army.setActive(false);
        when(armyRepository.findById(3L)).thenReturn(Optional.of(army));

        assertThatThrownBy(() -> armyService.useArmy(3L, 5L, OffsetDateTime.now(), "notes"))
                .isInstanceOf(BadRequestException.class);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void useArmyCreatesUsageAndPublishesNotification() {
        User owner = new User("Owner");
        ReflectionTestUtils.setField(owner, "id", 10L);
        Army army = new Army(owner, "Game", "Faction", false);
        ReflectionTestUtils.setField(army, "id", 7L);
        User usedBy = new User("Guest");
        ReflectionTestUtils.setField(usedBy, "id", 12L);

        when(armyRepository.findById(7L)).thenReturn(Optional.of(army));
        when(userRepository.findById(12L)).thenReturn(Optional.of(usedBy));
        when(usageRepository.save(any(ArmyUsage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OffsetDateTime usedAt = OffsetDateTime.now();
        ArmyUsage usage = armyService.useArmy(7L, 12L, usedAt, "note");

        assertThat(usage.getArmy()).isSameAs(army);
        assertThat(usage.getUsedBy()).isSameAs(usedBy);
        assertThat(usage.getUsedAt()).isEqualTo(usedAt);

        verify(loyaltyService).addPoints(10L);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(eventPublisher).publishEventNotification(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).contains("Game").contains("Faction").contains("Owner");
    }
}
