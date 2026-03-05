package com.wargameclub.clubapi.lab.service;

import java.util.concurrent.atomic.AtomicInteger;
import com.wargameclub.clubapi.lab.FaultMode;
import com.wargameclub.clubapi.lab.LabResilienceProperties;
import org.springframework.stereotype.Component;

/**
 * Объединяет query-параметры и env-конфигурацию в сценарий эмуляции.
 */
@Component
public class LabScenarioResolver {

    private final LabResilienceProperties properties;

    public LabScenarioResolver(LabResilienceProperties properties) {
        this.properties = properties;
    }

    public PurchaseScenarioOptions resolve(ScenarioOverrides overrides) {
        ServiceFaultSettings payment = new ServiceFaultSettings(
                defaultMode(overrides.paymentMode(), properties.getPayment().getMode()),
                positiveOrDefault(overrides.paymentDelayMs(), properties.getPayment().getSlowDelayMs()),
                probability(overrides.paymentErrorProbability(), properties.getPayment().getErrorProbability()),
                new AtomicInteger(nonNegative(overrides.paymentFailAttempts()))
        );

        ServiceFaultSettings notification = new ServiceFaultSettings(
                defaultMode(overrides.notificationMode(), properties.getNotification().getMode()),
                positiveOrDefault(overrides.notificationDelayMs(), properties.getNotification().getSlowDelayMs()),
                probability(overrides.notificationErrorProbability(), properties.getNotification().getErrorProbability()),
                new AtomicInteger(nonNegative(overrides.notificationFailAttempts()))
        );

        return new PurchaseScenarioOptions(payment, notification);
    }

    private FaultMode defaultMode(FaultMode requestMode, FaultMode defaultMode) {
        return requestMode != null ? requestMode : defaultMode;
    }

    private long positiveOrDefault(Long value, long defaultValue) {
        if (value == null || value <= 0) {
            return defaultValue;
        }
        return value;
    }

    private double probability(Double value, double defaultValue) {
        double resolved = value == null ? defaultValue : value;
        if (resolved < 0.0d) {
            return 0.0d;
        }
        if (resolved > 1.0d) {
            return 1.0d;
        }
        return resolved;
    }

    private int nonNegative(Integer value) {
        if (value == null || value < 0) {
            return 0;
        }
        return value;
    }
}
