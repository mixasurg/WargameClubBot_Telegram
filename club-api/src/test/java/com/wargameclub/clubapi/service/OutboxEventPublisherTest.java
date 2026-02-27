package com.wargameclub.clubapi.service;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherTest {

    @Mock
    private NotificationOutboxService outboxService;
    @Mock
    private TelegramSettingsService settingsService;

    private OutboxEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new OutboxEventPublisher(outboxService, settingsService);
    }

    @Test
    void publishEventNotificationEnqueuesWhenSettingsPresent() {
        ClubTelegramSettings settings = new ClubTelegramSettings(123L);
        settings.setScheduleThreadId(5);
        when(settingsService.getAny()).thenReturn(Optional.of(settings));

        publisher.publishEventNotification("Hello");

        ArgumentCaptor<ChatRouting> routingCaptor = ArgumentCaptor.forClass(ChatRouting.class);
        verify(outboxService).enqueue(
                NotificationTarget.TELEGRAM,
                routingCaptor.capture(),
                "Hello"
        );
        assertThat(routingCaptor.getValue().chatId()).isEqualTo(123L);
        assertThat(routingCaptor.getValue().threadId()).isEqualTo(5);
    }

    @Test
    void publishEventNotificationSkipsWhenNoSettings() {
        when(settingsService.getAny()).thenReturn(Optional.empty());

        publisher.publishEventNotification("Hello");

        verifyNoInteractions(outboxService);
    }
}
