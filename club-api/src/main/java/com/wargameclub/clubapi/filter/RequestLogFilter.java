package com.wargameclub.clubapi.filter;

import java.io.IOException;
import com.wargameclub.clubapi.service.RequestLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Класс модуля club-api.
 */
@Component
public class RequestLogFilter extends OncePerRequestFilter {

    /**
     * Сервис RequestLog.
     */
    private final RequestLogService requestLogService;

    /**
     * Конструктор RequestLogFilter.
     */
    public RequestLogFilter(RequestLogService requestLogService) {
        this.requestLogService = requestLogService;
    }

    /**
     * Выполняет операцию.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        StatusCaptureResponseWrapper responseWrapper = new StatusCaptureResponseWrapper(response);
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, responseWrapper);
        } catch (Exception ex) {
            responseWrapper.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw ex;
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            requestLogService.logRequest(
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getQueryString(),
                    responseWrapper.getStatus(),
                    durationMs,
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent")
            );
        }
    }

    /**
     * Класс модуля club-api.
     */
    private static class StatusCaptureResponseWrapper extends HttpServletResponseWrapper {

        /**
         * Поле состояния.
         */
        private int status = SC_OK;

        /**
         * Конструктор StatusCaptureResponseWrapper.
         */
        StatusCaptureResponseWrapper(HttpServletResponse response) {

            /**
             * Выполняет операцию.
             */
            super(response);
        }

        /**
         * Устанавливает Status.
         */
        @Override
        public void setStatus(int sc) {
            this.status = sc;
            super.setStatus(sc);
        }

        /**
         * Отправляет Error.
         */
        @Override
        public void sendError(int sc) throws IOException {
            this.status = sc;
            super.sendError(sc);
        }

        /**
         * Отправляет Error.
         */
        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.status = sc;
            super.sendError(sc, msg);
        }

        /**
         * Возвращает Status.
         */
        @Override
        public int getStatus() {
            return status;
        }
    }
}
