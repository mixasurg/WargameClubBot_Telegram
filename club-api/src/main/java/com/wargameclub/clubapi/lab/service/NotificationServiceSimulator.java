package com.wargameclub.clubapi.lab.service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import com.wargameclub.clubapi.lab.FaultMode;
import com.wargameclub.clubapi.lab.exception.HttpServiceException;
import com.wargameclub.clubapi.lab.exception.NetworkServiceException;
import org.springframework.stereotype.Component;

/**
 * Эмулятор внешнего сервиса уведомлений.
 */
@Component
public class NotificationServiceSimulator {

    private final AtomicInteger calls = new AtomicInteger();

    public void sendNotification(ServiceFaultSettings settings) {
        calls.incrementAndGet();

        if (settings.mode() == FaultMode.SLOW) {
            sleep(settings.slowDelayMs());
        }

        if (settings.mode() == FaultMode.DOWN) {
            throw new NetworkServiceException("Notification service unavailable");
        }

        if (settings.consumeForcedFailure()) {
            throw new HttpServiceException(503, "Notification service temporary unavailable");
        }

        if (settings.mode() == FaultMode.ERROR && ThreadLocalRandom.current().nextDouble() < settings.errorProbability()) {
            throw new HttpServiceException(500, "Notification service random internal error");
        }
    }

    public int getCalls() {
        return calls.get();
    }

    public void reset() {
        calls.set(0);
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(Math.max(delayMs, 0));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new NetworkServiceException("Notification service interrupted");
        }
    }
}
