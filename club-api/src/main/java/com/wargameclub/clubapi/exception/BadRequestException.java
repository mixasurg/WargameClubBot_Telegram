package com.wargameclub.clubapi.exception;

/**
 * Исключение для BadRequest.
 */
public class BadRequestException extends RuntimeException {

    /**
     * Конструктор BadRequestException.
     */
    public BadRequestException(String message) {

        /**
         * Выполняет операцию.
         */
        super(message);
    }
}

