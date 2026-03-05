package com.wargameclub.clubapi.lab;

import com.wargameclub.clubapi.lab.filter.LabRateLimitFilter;
import com.wargameclub.clubapi.lab.service.IpRateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class LabRateLimitFilterTest {

    private LabRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        LabResilienceProperties properties = new LabResilienceProperties();
        IpRateLimiterService ipRateLimiterService = new IpRateLimiterService(properties);
        filter = new LabRateLimitFilter(ipRateLimiterService);
    }

    @Test
    void sixthRequestFromSameIpIsRejectedWith429() throws Exception {
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/lab/tickets/purchase/limited");
            request.addHeader("X-Forwarded-For", "198.51.100.10");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, new MockFilterChain());

            assertThat(response.getStatus()).isEqualTo(200);
        }

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/lab/tickets/purchase/limited");
        request.addHeader("X-Forwarded-For", "198.51.100.10");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(429);
    }
}
