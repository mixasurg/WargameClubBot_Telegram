package com.wargameclub.clubapi.service;

import com.wargameclub.clubapi.entity.RequestLog;
import com.wargameclub.clubapi.repository.RequestLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestLogServiceTest {

    @Mock
    private RequestLogRepository repository;

    private RequestLogService service;

    @BeforeEach
    void setUp() {
        service = new RequestLogService(repository);
    }

    @Test
    void logRequestTruncatesFields() {
        String longValue = "x".repeat(500);

        service.logRequest(longValue, longValue, longValue, 200, 123, longValue, longValue);

        ArgumentCaptor<RequestLog> captor = ArgumentCaptor.forClass(RequestLog.class);
        verify(repository).save(captor.capture());
        RequestLog saved = captor.getValue();
        assertThat(saved.getMethod().length()).isLessThanOrEqualTo(10);
        assertThat(saved.getPath().length()).isLessThanOrEqualTo(300);
        assertThat(saved.getQuery().length()).isLessThanOrEqualTo(500);
        assertThat(saved.getRemoteAddr().length()).isLessThanOrEqualTo(100);
        assertThat(saved.getUserAgent().length()).isLessThanOrEqualTo(300);
    }

    @Test
    void logRequestSwallowsRepositoryErrors() {
        doThrow(new RuntimeException("boom")).when(repository).save(any(RequestLog.class));

        service.logRequest("GET", "/", null, 200, 1, null, null);

        verify(repository).save(any(RequestLog.class));
    }
}
