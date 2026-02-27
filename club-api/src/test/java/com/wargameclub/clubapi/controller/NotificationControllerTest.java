package com.wargameclub.clubapi.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import com.wargameclub.clubapi.dto.NotificationFailRequest;
import com.wargameclub.clubapi.dto.NotificationOutboxDto;
import com.wargameclub.clubapi.enums.NotificationStatus;
import com.wargameclub.clubapi.enums.NotificationTarget;
import com.wargameclub.clubapi.service.NotificationOutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationOutboxService notificationService;

    private NotificationController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationController(notificationService);
    }

    @Test
    void pendingReturnsDtos() {
        NotificationOutboxDto dto = new NotificationOutboxDto(
                UUID.randomUUID(),
                NotificationTarget.TELEGRAM,
                "route",
                "text",
                NotificationStatus.PENDING,
                0,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
        when(notificationService.getPending(NotificationTarget.TELEGRAM, 5)).thenReturn(List.of(dto));

        List<NotificationOutboxDto> result = controller.pending(NotificationTarget.TELEGRAM, 5);

        assertThat(result).containsExactly(dto);
    }

    @Test
    void ackDelegatesToService() {
        UUID id = UUID.randomUUID();

        controller.ack(id);

        verify(notificationService).markSent(id);
    }

    @Test
    void failDelegatesToService() {
        UUID id = UUID.randomUUID();

        controller.fail(id, new NotificationFailRequest("boom"));

        verify(notificationService).markFailed(id, "boom");
    }
}
