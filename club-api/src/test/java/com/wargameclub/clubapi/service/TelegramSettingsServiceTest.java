package com.wargameclub.clubapi.service;

import java.util.Optional;
import com.wargameclub.clubapi.entity.ClubTelegramSettings;
import com.wargameclub.clubapi.exception.NotFoundException;
import com.wargameclub.clubapi.repository.ClubTelegramSettingsRepository;
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
class TelegramSettingsServiceTest {

    @Mock
    private ClubTelegramSettingsRepository repository;

    private TelegramSettingsService service;

    @BeforeEach
    void setUp() {
        service = new TelegramSettingsService(repository);
    }

    @Test
    void getByChatIdThrowsWhenMissing() {
        when(repository.findByChatId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByChatId(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void upsertCreatesNewSettings() {
        when(repository.findByChatId(10L)).thenReturn(Optional.empty());
        when(repository.save(any(ClubTelegramSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClubTelegramSettings result = service.upsert(10L, 0, -1, 100, null, 200, " ");

        assertThat(result.getChatId()).isEqualTo(10L);
        assertThat(result.getScheduleThreadId()).isNull();
        assertThat(result.getEventsThreadId()).isNull();
        assertThat(result.getScheduleTwoweeksMessageId()).isEqualTo(100);
        assertThat(result.getEventsMessageId()).isEqualTo(200);
    }

    @Test
    void upsertUpdatesExistingSettings() {
        ClubTelegramSettings settings = new ClubTelegramSettings(10L);
        settings.setEventsThreadId(5);
        when(repository.findByChatId(10L)).thenReturn(Optional.of(settings));
        when(repository.save(any(ClubTelegramSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClubTelegramSettings result = service.upsert(10L, 12, null, null, 300, null, "UTC");

        assertThat(result.getScheduleThreadId()).isEqualTo(12);
        assertThat(result.getEventsThreadId()).isEqualTo(5);
        assertThat(result.getScheduleTwoweeksNextMessageId()).isEqualTo(300);
        assertThat(result.getTimezone()).isEqualTo("UTC");
        ArgumentCaptor<ClubTelegramSettings> captor = ArgumentCaptor.forClass(ClubTelegramSettings.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getScheduleThreadId()).isEqualTo(12);
    }
}
