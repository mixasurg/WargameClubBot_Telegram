package com.wargameclub.clubapi.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.wargameclub.clubapi.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that validates an API key for protected endpoints.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiKeyFilter extends OncePerRequestFilter {

    /**
     * Header that carries the API key.
     */
    private static final String API_KEY_HEADER = "X-API-KEY";

    /**
     * Application settings.
     */
    private final AppProperties appProperties;

    /**
     * Creates the API key filter.
     *
     * @param appProperties application settings
     */
    public ApiKeyFilter(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * Validates the API key and returns 401 if it is missing or invalid.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain filter chain
     * @throws ServletException servlet error
     * @throws IOException I/O error
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String expected = appProperties.getSecurity().getApiKey();
        if (expected == null || expected.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }
        String provided = request.getHeader(API_KEY_HEADER);
        if (provided == null || !provided.equals(expected)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/plain;charset=UTF-8");
            response.getOutputStream().write("Unauthorized".getBytes(StandardCharsets.UTF_8));
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Skips API key checks for technical endpoints and OPTIONS requests.
     *
     * @param request HTTP request
     * @return true if the filter should be skipped
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return path != null && (path.startsWith("/actuator") || path.startsWith("/error"));
    }
}
