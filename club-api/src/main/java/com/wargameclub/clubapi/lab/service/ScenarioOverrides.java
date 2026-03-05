package com.wargameclub.clubapi.lab.service;

import com.wargameclub.clubapi.lab.FaultMode;

/**
 * Переопределения сценария из query-параметров.
 */
public record ScenarioOverrides(
        FaultMode paymentMode,
        Long paymentDelayMs,
        Double paymentErrorProbability,
        Integer paymentFailAttempts,
        FaultMode notificationMode,
        Long notificationDelayMs,
        Double notificationErrorProbability,
        Integer notificationFailAttempts
) {
}
