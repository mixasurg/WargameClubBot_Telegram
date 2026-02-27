package com.wargameclub.clubapi.filter;

import java.util.concurrent.atomic.AtomicBoolean;
import com.wargameclub.clubapi.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyFilterTest {

    @Test
    void allowsWhenApiKeyNotConfigured() throws Exception {
        AppProperties props = new AppProperties();
        ApiKeyFilter filter = new ApiKeyFilter(props);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> invoked.set(true);

        filter.doFilter(request, response, chain);

        assertThat(invoked).isTrue();
    }

    @Test
    void rejectsWhenApiKeyMissing() throws Exception {
        AppProperties props = new AppProperties();
        props.getSecurity().setApiKey("secret");
        ApiKeyFilter filter = new ApiKeyFilter(props);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> invoked.set(true);

        filter.doFilter(request, response, chain);

        assertThat(invoked).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void rejectsWhenApiKeyInvalid() throws Exception {
        AppProperties props = new AppProperties();
        props.getSecurity().setApiKey("secret");
        ApiKeyFilter filter = new ApiKeyFilter(props);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader("X-API-KEY", "wrong");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> invoked.set(true);

        filter.doFilter(request, response, chain);

        assertThat(invoked).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void allowsWhenApiKeyMatches() throws Exception {
        AppProperties props = new AppProperties();
        props.getSecurity().setApiKey("secret");
        ApiKeyFilter filter = new ApiKeyFilter(props);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader("X-API-KEY", "secret");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> invoked.set(true);

        filter.doFilter(request, response, chain);

        assertThat(invoked).isTrue();
    }

    @Test
    void skipsActuatorEndpoints() throws Exception {
        AppProperties props = new AppProperties();
        props.getSecurity().setApiKey("secret");
        ApiKeyFilter filter = new ApiKeyFilter(props);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> invoked.set(true);

        filter.doFilter(request, response, chain);

        assertThat(invoked).isTrue();
    }

    @Test
    void skipsOptionsRequests() throws Exception {
        AppProperties props = new AppProperties();
        props.getSecurity().setApiKey("secret");
        ApiKeyFilter filter = new ApiKeyFilter(props);
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> invoked.set(true);

        filter.doFilter(request, response, chain);

        assertThat(invoked).isTrue();
    }
}
