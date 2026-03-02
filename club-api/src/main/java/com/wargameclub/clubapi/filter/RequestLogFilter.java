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
 * Фильтр, логирующий только ошибочные HTTP-запросы через {@link RequestLogService}.
 */
@Component
public class RequestLogFilter extends OncePerRequestFilter {

    /**
     * Сервис записи логов запросов.
     */
    private final RequestLogService requestLogService;
    /**
     * Нижняя граница HTTP-статуса, который считается ошибочным.
     */
    private static final int ERROR_STATUS_THRESHOLD = 400;

    /**
     * Создает фильтр логирования запросов.
     *
     * @param requestLogService сервис логирования
     */
    public RequestLogFilter(RequestLogService requestLogService) {
        this.requestLogService = requestLogService;
    }

    /**
     * Оборачивает ответ для захвата статуса, измеряет длительность и пишет лог запроса.
     *
     * @param request HTTP-запрос
     * @param response HTTP-ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException ошибка сервлета
     * @throws IOException ошибка ввода-вывода
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
            int status = responseWrapper.getStatus();
            if (status >= ERROR_STATUS_THRESHOLD) {
                requestLogService.logRequest(
                        request.getMethod(),
                        request.getRequestURI(),
                        request.getQueryString(),
                        status,
                        durationMs,
                        request.getRemoteAddr(),
                        request.getHeader("User-Agent")
                );
            }
        }
    }

    /**
     * Обертка {@link HttpServletResponse}, сохраняющая последний установленный статус.
     */
    private static class StatusCaptureResponseWrapper extends HttpServletResponseWrapper {

        /**
         * Последний установленный HTTP-статус.
         */
        private int status = SC_OK;

        /**
         * Создает обертку ответа.
         *
         * @param response исходный HTTP-ответ
         */
        StatusCaptureResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        /**
         * Устанавливает HTTP-статус и сохраняет его значение.
         *
         * @param sc HTTP-статус
         */
        @Override
        public void setStatus(int sc) {
            this.status = sc;
            super.setStatus(sc);
        }

        /**
         * Отправляет ошибку и сохраняет HTTP-статус.
         *
         * @param sc HTTP-статус
         * @throws IOException ошибка ввода-вывода
         */
        @Override
        public void sendError(int sc) throws IOException {
            this.status = sc;
            super.sendError(sc);
        }

        /**
         * Отправляет ошибку и сохраняет HTTP-статус.
         *
         * @param sc HTTP-статус
         * @param msg сообщение ошибки
         * @throws IOException ошибка ввода-вывода
         */
        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.status = sc;
            super.sendError(sc, msg);
        }

        /**
         * Возвращает последний установленный HTTP-статус.
         *
         * @return HTTP-статус
         */
        @Override
        public int getStatus() {
            return status;
        }
    }
}
