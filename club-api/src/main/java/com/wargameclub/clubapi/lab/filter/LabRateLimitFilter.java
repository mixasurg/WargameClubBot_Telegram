package com.wargameclub.clubapi.lab.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.wargameclub.clubapi.lab.service.IpRateLimiterService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Фильтр ограничения частоты запросов покупки билетов.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class LabRateLimitFilter extends OncePerRequestFilter {

    private static final String LIMITED_PATH = "/api/lab/tickets/purchase/limited";

    private final IpRateLimiterService ipRateLimiterService;

    public LabRateLimitFilter(IpRateLimiterService ipRateLimiterService) {
        this.ipRateLimiterService = ipRateLimiterService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String ip = resolveClientIp(request);
        if (!ipRateLimiterService.tryAcquire(ip)) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Too Many Requests\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod())
                || !LIMITED_PATH.equals(request.getRequestURI());
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
