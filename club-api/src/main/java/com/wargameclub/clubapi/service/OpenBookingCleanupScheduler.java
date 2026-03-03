package com.wargameclub.clubapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Планировщик автоснятия открытых броней с истекшим дедлайном.
 */
@Component
public class OpenBookingCleanupScheduler {

    /**
     * Логгер.
     */
    private static final Logger log = LoggerFactory.getLogger(OpenBookingCleanupScheduler.class);

    /**
     * Сервис бронирований.
     */
    private final BookingService bookingService;

    /**
     * Создает планировщик.
     *
     * @param bookingService сервис бронирований
     */
    public OpenBookingCleanupScheduler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Запускает автоотмену открытых броней каждые 5 минут.
     */
    @Scheduled(cron = "${app.booking.open-cleanup-cron:0 */5 * * * *}")
    public void cleanupExpiredOpenBookings() {
        int cancelled = bookingService.cancelExpiredOpenBookings();
        if (cancelled > 0) {
            log.info("Автоматически снято открытых броней: {}", cancelled);
        }
    }
}
