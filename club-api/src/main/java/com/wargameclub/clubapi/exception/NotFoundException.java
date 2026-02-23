package com.wargameclub.clubapi.exception;

/**
 * Исключение для NotFound.
 */
public class NotFoundException extends RuntimeException {

    /**
     * Конструктор NotFoundException.
     */
    public NotFoundException(String message) {

        /**
         * Выполняет операцию.
         */
        super(message);
    }
}

