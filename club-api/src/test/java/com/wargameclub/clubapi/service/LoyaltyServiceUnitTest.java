package com.wargameclub.clubapi.service;

import java.util.Optional;
import com.wargameclub.clubapi.config.AppProperties;
import com.wargameclub.clubapi.entity.LoyaltyAccount;
import com.wargameclub.clubapi.repository.LoyaltyAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceUnitTest {

    @Mock
    private LoyaltyAccountRepository repository;

    private AppProperties appProperties;
    private LoyaltyService service;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        appProperties.getLoyalty().setPointsArmyUsed(10);
        appProperties.getLoyalty().setPointsArmyShared(5);
        service = new LoyaltyService(repository, appProperties);
    }

    @Test
    void addPointsCreatesAccountWhenMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        when(repository.save(any(LoyaltyAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int points = service.addPoints(1L);

        assertThat(points).isEqualTo(10);
        ArgumentCaptor<LoyaltyAccount> captor = ArgumentCaptor.forClass(LoyaltyAccount.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPoints()).isEqualTo(10);
    }

    @Test
    void addPointsForSharedArmyUsesConfiguredValue() {
        LoyaltyAccount account = new LoyaltyAccount(1L, 20);
        when(repository.findById(1L)).thenReturn(Optional.of(account));

        int points = service.addPointsForSharedArmy(1L);

        assertThat(points).isEqualTo(25);
        verify(repository).save(account);
    }

    @Test
    void getPointsReturnsZeroWhenMissing() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        int points = service.getPoints(2L);

        assertThat(points).isEqualTo(0);
    }
}
