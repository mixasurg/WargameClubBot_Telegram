package com.wargameclub.clubapi.lab.service;

import com.wargameclub.clubapi.lab.dto.LabTicketPurchaseRequest;
import com.wargameclub.clubapi.lab.dto.LabTicketPurchaseResponse;
import org.springframework.stereotype.Service;

/**
 * Оркестратор лабораторного сценария покупки билета.
 */
@Service
public class LabTicketPurchaseService {

    private static final String NOTIFICATION_FALLBACK_MESSAGE = "Билет забронирован, уведомление будет отправлено позже";

    private final LabScenarioResolver scenarioResolver;
    private final PaymentResilienceService paymentResilienceService;
    private final NotificationResilienceService notificationResilienceService;
    private final PaymentServiceSimulator paymentServiceSimulator;
    private final NotificationServiceSimulator notificationServiceSimulator;
    private final NotificationFallbackQueue fallbackQueue;

    public LabTicketPurchaseService(
            LabScenarioResolver scenarioResolver,
            PaymentResilienceService paymentResilienceService,
            NotificationResilienceService notificationResilienceService,
            PaymentServiceSimulator paymentServiceSimulator,
            NotificationServiceSimulator notificationServiceSimulator,
            NotificationFallbackQueue fallbackQueue
    ) {
        this.scenarioResolver = scenarioResolver;
        this.paymentResilienceService = paymentResilienceService;
        this.notificationResilienceService = notificationResilienceService;
        this.paymentServiceSimulator = paymentServiceSimulator;
        this.notificationServiceSimulator = notificationServiceSimulator;
        this.fallbackQueue = fallbackQueue;
    }

    public LabTicketPurchaseResponse purchase(LabTicketPurchaseRequest request, ScenarioOverrides overrides) {
        PurchaseScenarioOptions options = scenarioResolver.resolve(overrides);

        PaymentExecutionResult payment = paymentResilienceService.processPayment(request.userId(), options.payment());
        if (!payment.success()) {
            return response(false, payment.message(), payment.status(), "NOT_EXECUTED", payment.attempts());
        }

        NotificationExecutionResult notification = notificationResilienceService.sendNotification(
                request.userId(),
                request.ticketCode(),
                options.notification()
        );

        if (!notification.delivered()) {
            return response(true, NOTIFICATION_FALLBACK_MESSAGE, payment.status(), notification.status(), payment.attempts());
        }

        return response(true, "Билет забронирован и уведомление отправлено", payment.status(), notification.status(), payment.attempts());
    }

    private LabTicketPurchaseResponse response(
            boolean ticketBooked,
            String message,
            String paymentStatus,
            String notificationStatus,
            int paymentAttempts
    ) {
        return new LabTicketPurchaseResponse(
                ticketBooked,
                message,
                paymentStatus,
                notificationStatus,
                paymentAttempts,
                paymentResilienceService.getCircuitState(),
                paymentServiceSimulator.getCalls(),
                notificationServiceSimulator.getCalls(),
                fallbackQueue.size()
        );
    }
}
