package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import java.util.Optional;
import com.wargameclub.clubapi.entity.ClubTelegramSettings;
import com.wargameclub.clubapi.enums.NotificationTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramAutoRefreshServiceTest {

    @Mock
    private NotificationOutboxService outboxService;
    @Mock
    private TelegramSettingsService settingsService;

    private TelegramAutoRefreshService service;

    @BeforeEach
    void setUp() {
        service = new TelegramAutoRefreshService(outboxService, settingsService);
    }

    @Test
    void refreshTwoweeksIfWithinRangeEnqueuesCommand() {
        ClubTelegramSettings settings = new ClubTelegramSettings(42L);
        settings.setScheduleThreadId(7);
        when(settingsService.getAny()).thenReturn(Optional.of(settings));

        service.refreshTwoweeksIfWithinRange(OffsetDateTime.now().plusDays(5));

        ArgumentCaptor<ChatRouting> routingCaptor = ArgumentCaptor.forClass(ChatRouting.class);
        verify(outboxService).enqueue(
                eq(NotificationTarget.TELEGRAM),
                routingCaptor.capture(),
                eq(TelegramNotificationCommand.REFRESH_TWOWEEKS)
        );
        assertThat(routingCaptor.getValue().chatId()).isEqualTo(42L);
        assertThat(routingCaptor.getValue().threadId()).isEqualTo(7);
    }

    @Test
    void refreshTwoweeksIfWithinRangeSkipsWhenOutOfRange() {
        service.refreshTwoweeksIfWithinRange(OffsetDateTime.now().plusWeeks(3));

        verifyNoInteractions(outboxService);
    }

    @Test
    void refreshEventsEnqueuesCommandWhenSettingsPresent() {
        ClubTelegramSettings settings = new ClubTelegramSettings(100L);
        settings.setScheduleThreadId(2);
        when(settingsService.getAny()).thenReturn(Optional.of(settings));

        service.refreshEvents();

        verify(outboxService).enqueue(
                NotificationTarget.TELEGRAM,
                new ChatRouting(100L, 2),
                TelegramNotificationCommand.REFRESH_EVENTS
        );
    }
}
