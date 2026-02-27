package com.wargameclub.clubapi.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wargameclub.clubapi.config.AppProperties;
import com.wargameclub.clubapi.dto.NotificationOutboxDto;
import com.wargameclub.clubapi.entity.NotificationOutbox;
import com.wargameclub.clubapi.enums.NotificationStatus;
import com.wargameclub.clubapi.enums.NotificationTarget;
import com.wargameclub.clubapi.exception.BadRequestException;
import com.wargameclub.clubapi.exception.NotFoundException;
import com.wargameclub.clubapi.repository.NotificationOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxServiceTest {

    @Mock
    private NotificationOutboxRepository repository;

    private AppProperties appProperties;
    private ObjectMapper objectMapper;
    private NotificationOutboxService service;

    @BeforeEach
    void setUp() {
        appProperties = new AppProperties();
        appProperties.getNotifications().setMaxAttempts(2);
        appProperties.getNotifications().setBackoffSeconds(60);
        objectMapper = new ObjectMapper();
        service = new NotificationOutboxService(repository, objectMapper, appProperties);
    }

    @Test
    void enqueueAtRequiresRouting() {
        assertThatThrownBy(() -> service.enqueueAt(NotificationTarget.TELEGRAM, null, "text", null, null, null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void enqueueAtSerializesRouting() {
        when(repository.save(any(NotificationOutbox.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatRouting routing = new ChatRouting(123L, 5);
        NotificationOutbox outbox = service.enqueueAt(
                NotificationTarget.TELEGRAM,
                routing,
                "hello",
                OffsetDateTime.now().plusMinutes(5),
                "BOOKING",
                10L
        );

        assertThat(outbox.getChatRouting()).contains("123");
        assertThat(outbox.getReferenceType()).isEqualTo("BOOKING");
        assertThat(outbox.getReferenceId()).isEqualTo(10L);
    }

    @Test
    void enqueueAtThrowsOnSerializationFailure() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("boom") {
        });
        NotificationOutboxService failingService = new NotificationOutboxService(repository, failingMapper, appProperties);

        assertThatThrownBy(() -> failingService.enqueueAt(
                NotificationTarget.TELEGRAM,
                new ChatRouting(1L, null),
                "text",
                null,
                null,
                null
        )).isInstanceOf(BadRequestException.class)
                ;
    }

    @Test
    void getPendingMapsToDtos() {
        NotificationOutbox outbox = new NotificationOutbox(UUID.randomUUID(), NotificationTarget.TELEGRAM, "route", "text");
        when(repository.findByTargetAndStatusAndNextAttemptAtLessThanEqual(
                eq(NotificationTarget.TELEGRAM),
                eq(NotificationStatus.PENDING),
                any(OffsetDateTime.class),
                eq(PageRequest.of(0, 10))
        )).thenReturn(new PageImpl<>(List.of(outbox)));

        List<NotificationOutboxDto> result = service.getPending(NotificationTarget.TELEGRAM, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(outbox.getId());
    }

    @Test
    void markSentUpdatesStatusAndClearsError() {
        UUID id = UUID.randomUUID();
        NotificationOutbox outbox = new NotificationOutbox(id, NotificationTarget.TELEGRAM, "route", "text");
        outbox.setLastError("fail");
        when(repository.findById(id)).thenReturn(Optional.of(outbox));

        service.markSent(id);

        assertThat(outbox.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(outbox.getSentAt()).isNotNull();
        assertThat(outbox.getLastError()).isNull();
    }

    @Test
    void markFailedSchedulesRetryBeforeMaxAttempts() {
        UUID id = UUID.randomUUID();
        NotificationOutbox outbox = new NotificationOutbox(id, NotificationTarget.TELEGRAM, "route", "text");
        outbox.setAttempts(0);
        when(repository.findById(id)).thenReturn(Optional.of(outbox));

        service.markFailed(id, "error");

        assertThat(outbox.getAttempts()).isEqualTo(1);
        assertThat(outbox.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(outbox.getNextAttemptAt()).isAfter(OffsetDateTime.now().minusSeconds(1));
    }

    @Test
    void markFailedMarksFailedWhenMaxAttemptsReached() {
        UUID id = UUID.randomUUID();
        NotificationOutbox outbox = new NotificationOutbox(id, NotificationTarget.TELEGRAM, "route", "text");
        outbox.setAttempts(1);
        when(repository.findById(id)).thenReturn(Optional.of(outbox));

        service.markFailed(id, "error");

        assertThat(outbox.getAttempts()).isEqualTo(2);
        assertThat(outbox.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    void markSentThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markSent(id))
                .isInstanceOf(NotFoundException.class);
    }
}
