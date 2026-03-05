package com.wargameclub.clubapi.lab.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.wargameclub.clubapi.lab.LabResilienceProperties;
import org.springframework.stereotype.Service;

/**
 * Ограничение частоты запросов по IP-адресу.
 */
@Service
public class IpRateLimiterService {

    private final Map<String, Deque<Long>> requestsByIp = new ConcurrentHashMap<>();
    private final LabResilienceProperties properties;

    public IpRateLimiterService(LabResilienceProperties properties) {
        this.properties = properties;
    }

    public boolean tryAcquire(String ip) {
        String key = ip == null || ip.isBlank() ? "unknown" : ip;
        Deque<Long> queue = requestsByIp.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (queue) {
            long now = System.currentTimeMillis();
            long windowMs = Math.max(1, properties.getRateLimiter().getWindowMs());
            while (!queue.isEmpty() && now - queue.peekFirst() >= windowMs) {
                queue.removeFirst();
            }

            if (queue.size() >= Math.max(1, properties.getRateLimiter().getMaxRequestsPerMinute())) {
                return false;
            }

            queue.addLast(now);
            return true;
        }
    }

    public void reset() {
        requestsByIp.clear();
    }
}
