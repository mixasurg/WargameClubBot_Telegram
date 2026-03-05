package com.wargameclub.clubapi.lab.service;

import java.util.concurrent.atomic.AtomicInteger;
import com.wargameclub.clubapi.lab.FaultMode;

/**
 * Настройки эмуляции отказов для одного запроса.
 */
public record ServiceFaultSettings(
        FaultMode mode,
        long slowDelayMs,
        double errorProbability,
        AtomicInteger failAttemptsLeft
) {

    public boolean consumeForcedFailure() {
        if (failAttemptsLeft == null) {
            return false;
        }
        int current = failAttemptsLeft.getAndUpdate(value -> value > 0 ? value - 1 : value);
        return current > 0;
    }
}
