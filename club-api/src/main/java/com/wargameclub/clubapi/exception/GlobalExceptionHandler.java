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

/**
 * Класс модуля club-api.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Логгер.
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Обрабатывает NotFound.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    /**
     * Обрабатывает Conflict.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    /**
     * Обрабатывает BadRequest.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    /**
     * Обрабатывает Validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());
        return buildError(HttpStatus.BAD_REQUEST, "Ошибка валидации", request, details);
    }

    /**
     * Обрабатывает Constraint.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());
        return buildError(HttpStatus.BAD_REQUEST, "Ошибка валидации", request, details);
    }

    /**
     * Обрабатывает TypeMismatch.
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
     * Обрабатывает Unexpected.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Необработанная ошибка", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Непредвиденная ошибка", request, null);
    }

    /**
     * Формирует Error.
     */
    private ResponseEntity<ApiError> buildError(HttpStatus status, String message, HttpServletRequest request, List<String> details) {

        /**
         * Выполняет операцию.
         */
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
     * Определяет StatusMessage.
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
     * Форматирует FieldError.
     */
    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}

