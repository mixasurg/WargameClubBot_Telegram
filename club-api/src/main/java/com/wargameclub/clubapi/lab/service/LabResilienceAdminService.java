package com.wargameclub.clubapi.lab.service;

import com.wargameclub.clubapi.lab.dto.LabResilienceStatsDto;
import org.springframework.stereotype.Service;

/**
 * Сервис диагностики и сброса состояния лабораторного контура.
 */
@Service
public class LabResilienceAdminService {

    private final PaymentResilienceService paymentResilienceService;
    private final PaymentServiceSimulator paymentServiceSimulator;
    private final NotificationServiceSimulator notificationServiceSimulator;
    private final NotificationFallbackQueue fallbackQueue;
    private final IpRateLimiterService ipRateLimiterService;

    public LabResilienceAdminService(
            PaymentResilienceService paymentResilienceService,
            PaymentServiceSimulator paymentServiceSimulator,
            NotificationServiceSimulator notificationServiceSimulator,
            NotificationFallbackQueue fallbackQueue,
            IpRateLimiterService ipRateLimiterService
    ) {
        this.paymentResilienceService = paymentResilienceService;
        this.paymentServiceSimulator = paymentServiceSimulator;
        this.notificationServiceSimulator = notificationServiceSimulator;
        this.fallbackQueue = fallbackQueue;
        this.ipRateLimiterService = ipRateLimiterService;
    }

    public LabResilienceStatsDto stats() {
        return new LabResilienceStatsDto(
                paymentResilienceService.getCircuitState(),
                paymentServiceSimulator.getCalls(),
                notificationServiceSimulator.getCalls(),
                fallbackQueue.size()
        );
    }

    public LabResilienceStatsDto resetState() {
        paymentResilienceService.reset();
        paymentServiceSimulator.reset();
        notificationServiceSimulator.reset();
        fallbackQueue.clear();
        ipRateLimiterService.reset();
        return stats();
    }
}
