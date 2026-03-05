package com.wargameclub.clubapi.lab.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Внутренняя очередь отложенных уведомлений для ручной отправки.
 */
@Component
public class NotificationFallbackQueue {

    private static final Logger log = LoggerFactory.getLogger(NotificationFallbackQueue.class);

    private final Queue<String> queue = new ConcurrentLinkedQueue<>();

    public void enqueue(Long userId, String ticketCode, String reason) {
        String item = OffsetDateTime.now() + " | userId=" + userId + " | ticket=" + ticketCode + " | reason=" + reason;
        queue.add(item);
        log.warn("Notification queued for manual send: {}", item);
    }

    public int size() {
        return queue.size();
    }

    public List<String> snapshot() {
        return new ArrayList<>(queue);
    }

    public void clear() {
        queue.clear();
    }
}
