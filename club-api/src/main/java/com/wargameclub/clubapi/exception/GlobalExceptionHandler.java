package com.wargameclub.clubapi.exception;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Глобальный обработчик исключений REST API, формирующий ответ {@link ApiError}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Логгер.
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Обрабатывает случаи, когда ресурс не найден.
     *
     * @param ex исключение о ненайденном ресурсе
     * @param request HTTP-запрос
     * @return ответ с ошибкой 404
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    /**
     * Обрабатывает конфликтные состояния.
     *
     * @param ex исключение конфликта
     * @param request HTTP-запрос
     * @return ответ с ошибкой 409
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    /**
     * Обрабатывает некорректные запросы.
     *
     * @param ex исключение некорректного запроса
     * @param request HTTP-запрос
     * @return ответ с ошибкой 400
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    /**
     * Обрабатывает исключения с явным HTTP-статусом.
     *
     * @param ex исключение с HTTP-статусом
     * @param request HTTP-запрос
     * @return ответ с указанным статусом
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason() == null ? status.getReasonPhrase() : ex.getReason();
        return buildError(status, message, request, null);
    }

    /**
     * Обрабатывает ошибки валидации тела запроса (@Valid).
     *
     * @param ex исключение валидации
     * @param request HTTP-запрос
     * @return ответ с ошибкой 400 и деталями по полям
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());
        return buildError(HttpStatus.BAD_REQUEST, "Ошибка валидации", request, details);
    }

    /**
     * Обрабатывает ошибки валидации параметров запроса.
     *
     * @param ex исключение ограничений
     * @param request HTTP-запрос
     * @return ответ с ошибкой 400 и деталями
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());
        return buildError(HttpStatus.BAD_REQUEST, "Ошибка валидации", request, details);
    }

    /**
     * Обрабатывает случаи неверного формата параметров запроса.
     *
     * @param ex исключение несовпадения типа
     * @param request HTTP-запрос
     * @return ответ с ошибкой 400 и подсказкой формата при необходимости
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Некорректный формат параметра: " + ex.getName();
        String hint = "";
        if (ex.getRequiredType() != null && ex.getRequiredType().getSimpleName().contains("OffsetDateTime")) {
            hint = " Используйте формат ISO-8601, например 2026-02-17T00:00:00%2B02:00.";
        }
        List<String> details = List.of(ex.getMessage() + hint);
        return buildError(HttpStatus.BAD_REQUEST, message, request, details);
    }

    /**
     * Обрабатывает все необработанные исключения.
     *
     * @param ex исключение
     * @param request HTTP-запрос
     * @return ответ с ошибкой 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Необработанная ошибка", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Непредвиденная ошибка", request, null);
    }

    /**
     * Собирает {@link ApiError} и упаковывает его в {@link ResponseEntity}.
     *
     * @param status HTTP-статус
     * @param message сообщение об ошибке
     * @param request HTTP-запрос
     * @param details дополнительные детали (опционально)
     * @return ответ с телом ошибки
     */
    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<String> details
    ) {
        ApiError error = new ApiError(
                OffsetDateTime.now(),
                status.value(),
                resolveStatusMessage(status),
                message,
                request.getRequestURI(),
                details
        );
        return new ResponseEntity<>(error, status);
    }

    /**
     * Возвращает локализованное описание HTTP-статуса.
     *
     * @param status HTTP-статус
     * @return текстовое описание статуса
     */
    private String resolveStatusMessage(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Неверный запрос";
            case NOT_FOUND -> "Не найдено";
            case CONFLICT -> "Конфликт";
            case INTERNAL_SERVER_ERROR -> "Внутренняя ошибка сервера";
            default -> status.getReasonPhrase();
        };
    }

    /**
     * Форматирует сообщение ошибки по полю.
     *
     * @param fieldError ошибка поля
     * @return строка вида "field: message"
     */
    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
