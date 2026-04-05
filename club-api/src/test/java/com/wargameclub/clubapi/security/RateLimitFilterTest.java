package com.wargameclub.clubapi.security;

import java.util.concurrent.atomic.AtomicInteger;
import com.wargameclub.clubapi.config.AppProperties;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты фильтра rate limiting.
 */
class RateLimitFilterTest {

    @Test
    void allowsRequestWhenRateLimitDisabled() throws Exception {
        AppProperties properties = new AppProperties();
        properties.getSecurity().setRateLimitEnabled(false);
        RateLimitFilter filter = new RateLimitFilter(properties);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/games");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger chainCalls = new AtomicInteger(0);
        FilterChain chain = (req, res) -> chainCalls.incrementAndGet();

        filter.doFilter(request, response, chain);

        assertThat(chainCalls.get()).isEqualTo(1);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void blocksSecondLoginAttemptWhenLimitExceeded() throws Exception {
        AppProperties properties = new AppProperties();
        properties.getSecurity().setRateLimitEnabled(true);
        properties.getSecurity().setLoginRateLimitPerMinute(1);
        properties.getSecurity().setRateLimitPerMinute(100);
        RateLimitFilter filter = new RateLimitFilter(properties);

        AtomicInteger chainCalls = new AtomicInteger(0);
        FilterChain chain = (req, res) -> chainCalls.incrementAndGet();

        MockHttpServletRequest first = new MockHttpServletRequest("POST", "/api/auth/login");
        first.setRemoteAddr("10.10.10.1");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(first, firstResponse, chain);

        MockHttpServletRequest second = new MockHttpServletRequest("POST", "/api/auth/login");
        second.setRemoteAddr("10.10.10.1");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(second, secondResponse, chain);

        assertThat(chainCalls.get()).isEqualTo(1);
        assertThat(firstResponse.getStatus()).isEqualTo(200);
        assertThat(secondResponse.getStatus()).isEqualTo(429);
        assertThat(secondResponse.getHeader("Retry-After")).isNotBlank();
    }
}
