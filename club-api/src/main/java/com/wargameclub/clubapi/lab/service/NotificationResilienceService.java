package com.wargameclub.clubapi.lab.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.wargameclub.clubapi.lab.LabResilienceProperties;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

/**
 * Вызывает сервис уведомлений через Thread Pool Bulkhead + Fallback.
 */
@Service
public class NotificationResilienceService {

    private final NotificationServiceSimulator notificationServiceSimulator;
    private final NotificationFallbackQueue fallbackQueue;
    private final ThreadPoolExecutor bulkheadExecutor;

    public NotificationResilienceService(
            LabResilienceProperties properties,
            NotificationServiceSimulator notificationServiceSimulator,
            NotificationFallbackQueue fallbackQueue
    ) {
        this.notificationServiceSimulator = notificationServiceSimulator;
        this.fallbackQueue = fallbackQueue;

        int maxConcurrent = Math.max(1, properties.getBulkhead().getMaxConcurrentCalls());
        int queueCapacity = Math.max(0, properties.getBulkhead().getQueueCapacity());

        BlockingQueue<Runnable> queue = queueCapacity == 0
                ? new SynchronousQueue<>()
                : new ArrayBlockingQueue<>(queueCapacity);

        this.bulkheadExecutor = new ThreadPoolExecutor(
                maxConcurrent,
                maxConcurrent,
                0,
                TimeUnit.MILLISECONDS,
                queue,
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public NotificationExecutionResult sendNotification(Long userId, String ticketCode, ServiceFaultSettings settings) {
        Future<Void> future;
        try {
            future = bulkheadExecutor.submit(() -> {
                notificationServiceSimulator.sendNotification(settings);
                return null;
            });
        } catch (RejectedExecutionException ex) {
            fallbackQueue.enqueue(userId, ticketCode, "BULKHEAD_REJECTED");
            return new NotificationExecutionResult(false, "QUEUED_FALLBACK");
        }

        try {
            future.get();
            return new NotificationExecutionResult(true, "SENT");
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            fallbackQueue.enqueue(userId, ticketCode, cause == null ? "UNKNOWN" : cause.getMessage());
            return new NotificationExecutionResult(false, "QUEUED_FALLBACK");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            fallbackQueue.enqueue(userId, ticketCode, "INTERRUPTED");
            return new NotificationExecutionResult(false, "QUEUED_FALLBACK");
        }
    }

    @PreDestroy
    void shutdown() {
        bulkheadExecutor.shutdownNow();
    }
}
