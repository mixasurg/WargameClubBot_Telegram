package com.wargameclub.clubapi.lab;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.wargameclub.clubapi.lab.dto.LabResilienceStatsDto;
import com.wargameclub.clubapi.lab.dto.LabTicketPurchaseRequest;
import com.wargameclub.clubapi.lab.dto.LabTicketPurchaseResponse;
import com.wargameclub.clubapi.lab.service.IpRateLimiterService;
import com.wargameclub.clubapi.lab.service.LabResilienceAdminService;
import com.wargameclub.clubapi.lab.service.LabScenarioResolver;
import com.wargameclub.clubapi.lab.service.LabTicketPurchaseService;
import com.wargameclub.clubapi.lab.service.NotificationFallbackQueue;
import com.wargameclub.clubapi.lab.service.NotificationResilienceService;
import com.wargameclub.clubapi.lab.service.NotificationServiceSimulator;
import com.wargameclub.clubapi.lab.service.PaymentResilienceService;
import com.wargameclub.clubapi.lab.service.PaymentServiceSimulator;
import com.wargameclub.clubapi.lab.service.ScenarioOverrides;
import com.wargameclub.clubapi.service.LoyaltyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class LabTicketPurchaseServiceTest {

    private LabTicketPurchaseService purchaseService;
    private LabResilienceAdminService adminService;
    private PaymentResilienceService paymentResilienceService;
    private NotificationResilienceService notificationResilienceService;

    @BeforeEach
    void setUp() {
        LabResilienceProperties properties = new LabResilienceProperties();
        LoyaltyService loyaltyService = Mockito.mock(LoyaltyService.class);
        PaymentServiceSimulator paymentServiceSimulator = new PaymentServiceSimulator(loyaltyService);
        NotificationServiceSimulator notificationServiceSimulator = new NotificationServiceSimulator();
        NotificationFallbackQueue fallbackQueue = new NotificationFallbackQueue();

        paymentResilienceService = new PaymentResilienceService(properties, paymentServiceSimulator);
        notificationResilienceService = new NotificationResilienceService(
                properties,
                notificationServiceSimulator,
                fallbackQueue
        );

        LabScenarioResolver resolver = new LabScenarioResolver(properties);
        purchaseService = new LabTicketPurchaseService(
                resolver,
                paymentResilienceService,
                notificationResilienceService,
                paymentServiceSimulator,
                notificationServiceSimulator,
                fallbackQueue
        );

        IpRateLimiterService rateLimiterService = new IpRateLimiterService(properties);
        adminService = new LabResilienceAdminService(
                paymentResilienceService,
                paymentServiceSimulator,
                notificationServiceSimulator,
                fallbackQueue,
                rateLimiterService
        );
        adminService.resetState();
    }

    @AfterEach
    void tearDown() {
        ReflectionTestUtils.invokeMethod(paymentResilienceService, "shutdown");
        ReflectionTestUtils.invokeMethod(notificationResilienceService, "shutdown");
    }

    @Test
    void timeoutStopsPaymentCallAfterTwoSeconds() {
        long startedAt = System.currentTimeMillis();

        LabTicketPurchaseResponse response = purchaseService.purchase(
                request(1L),
                new ScenarioOverrides(FaultMode.SLOW, 5000L, null, null, null, null, null, null)
        );

        long elapsedMs = System.currentTimeMillis() - startedAt;

        assertThat(response.ticketBooked()).isFalse();
        assertThat(response.message()).isEqualTo("Оплата не прошла, попробуйте позже");
        assertThat(response.paymentAttempts()).isEqualTo(1);
        assertThat(elapsedMs).isLessThan(3500);
    }

    @Test
    void retrySucceedsWhenFirstAttemptReturns503() {
        LabTicketPurchaseResponse response = purchaseService.purchase(
                request(2L),
                new ScenarioOverrides(FaultMode.NORMAL, null, null, 1, null, null, null, null)
        );

        assertThat(response.ticketBooked()).isTrue();
        assertThat(response.paymentStatus()).isEqualTo("SUCCESS");
        assertThat(response.paymentAttempts()).isEqualTo(2);
    }

    @Test
    void circuitBreakerOpensThenMovesHalfOpenAndClosesAfterRecovery() throws Exception {
        ScenarioOverrides downOverrides = new ScenarioOverrides(
                FaultMode.DOWN,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        for (int i = 0; i < 10; i++) {
            purchaseService.purchase(request(100L + i), downOverrides);
        }

        LabResilienceStatsDto openStats = adminService.stats();
        assertThat(openStats.paymentCircuitState()).isEqualTo(CircuitState.OPEN);

        int callsBeforeOpenFallback = openStats.paymentServiceCalls();
        LabTicketPurchaseResponse fallback = purchaseService.purchase(request(500L), downOverrides);
        assertThat(fallback.paymentStatus()).isEqualTo("FALLBACK_CIRCUIT_OPEN");
        assertThat(adminService.stats().paymentServiceCalls()).isEqualTo(callsBeforeOpenFallback);

        Thread.sleep(10500);

        for (int i = 0; i < 5; i++) {
            LabTicketPurchaseResponse recovered = purchaseService.purchase(
                    request(700L + i),
                    new ScenarioOverrides(null, null, null, null, null, null, null, null)
            );
            assertThat(recovered.ticketBooked()).isTrue();
        }

        assertThat(adminService.stats().paymentCircuitState()).isEqualTo(CircuitState.CLOSED);
    }

    @Test
    void bulkheadAllowsOnlyTwoParallelNotificationCalls() throws Exception {
        ScenarioOverrides overrides = new ScenarioOverrides(
                FaultMode.NORMAL,
                null,
                null,
                null,
                FaultMode.SLOW,
                1500L,
                null,
                null
        );

        int parallelRequests = 5;
        ExecutorService executor = Executors.newFixedThreadPool(parallelRequests);
        CountDownLatch ready = new CountDownLatch(parallelRequests);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<LabTicketPurchaseResponse>> futures = new ArrayList<>();
        for (int i = 0; i < parallelRequests; i++) {
            long userId = 800L + i;
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await(3, TimeUnit.SECONDS);
                return purchaseService.purchase(request(userId), overrides);
            }));
        }

        ready.await(3, TimeUnit.SECONDS);
        start.countDown();

        List<LabTicketPurchaseResponse> responses = new ArrayList<>();
        for (Future<LabTicketPurchaseResponse> future : futures) {
            responses.add(future.get(6, TimeUnit.SECONDS));
        }
        executor.shutdownNow();

        long delivered = responses.stream().filter(response -> "SENT".equals(response.notificationStatus())).count();
        long queued = responses.stream().filter(response -> "QUEUED_FALLBACK".equals(response.notificationStatus())).count();

        assertThat(delivered).isEqualTo(2);
        assertThat(queued).isEqualTo(3);

        LabResilienceStatsDto stats = adminService.stats();
        assertThat(stats.notificationServiceCalls()).isEqualTo(2);
        assertThat(stats.queuedNotifications()).isEqualTo(3);
    }

    @Test
    void notificationFallbackQueuesMessageAndReturnsSuccessfulBooking() {
        LabTicketPurchaseResponse response = purchaseService.purchase(
                request(77L),
                new ScenarioOverrides(
                        FaultMode.NORMAL,
                        null,
                        null,
                        null,
                        FaultMode.DOWN,
                        null,
                        null,
                        null
                )
        );

        assertThat(response.ticketBooked()).isTrue();
        assertThat(response.message()).isEqualTo("Билет забронирован, уведомление будет отправлено позже");
        assertThat(response.notificationStatus()).isEqualTo("QUEUED_FALLBACK");
        assertThat(adminService.stats().queuedNotifications()).isEqualTo(1);
    }

    @Test
    void queryOverridesCanForceRandomErrorMode() {
        LabTicketPurchaseResponse response = purchaseService.purchase(
                request(90L),
                new ScenarioOverrides(FaultMode.ERROR, null, 1.0d, null, null, null, null, null)
        );

        assertThat(response.ticketBooked()).isFalse();
        assertThat(response.message()).isEqualTo("Оплата не прошла, попробуйте позже");
    }

    private LabTicketPurchaseRequest request(Long userId) {
        return new LabTicketPurchaseRequest(userId, "ticket-" + userId, new BigDecimal("100.00"));
    }
}
