package com.anurag.backend;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableScheduling
public class BookingExpirationScheduler {

    private final BookingRepository bookingRepository;

    public BookingExpirationScheduler(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void completeExpiredBookings() {
        try {
            bookingRepository.completeExpiredBookings(LocalDateTime.now());
        } catch (Exception ex) {
            // Log quietly if database is temporarily unavailable during scheduled job
        }
    }
}