package com.wargameclub.clubapi.filter;

import com.wargameclub.clubapi.service.RequestLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestLogFilterUnitTest {

    @Mock
    private RequestLogService requestLogService;

    private RequestLogFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RequestLogFilter(requestLogService);
    }

    @Test
    void doesNotLogSuccessfulStatus() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(201);

        filter.doFilter(request, response, chain);

        verify(requestLogService, never()).logRequest(
                eq("GET"),
                eq("/api/test"),
                isNull(),
                eq(201),
                anyLong(),
                eq("127.0.0.1"),
                eq("JUnit")
        );
    }

    @Test
    void logsErrorStatusFromResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(404);

        filter.doFilter(request, response, chain);

        verify(requestLogService).logRequest(
                eq("GET"),
                eq("/api/test"),
                isNull(),
                eq(404),
                anyLong(),
                eq("127.0.0.1"),
                eq("JUnit")
        );
    }

    @Test
    void logsInternalServerErrorOnException() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            throw new RuntimeException("boom");
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isInstanceOf(RuntimeException.class);

        verify(requestLogService).logRequest(
                eq("GET"),
                eq("/api/test"),
                isNull(),
                eq(500),
                anyLong(),
                eq("10.0.0.1"),
                eq("JUnit")
        );
    }
}
