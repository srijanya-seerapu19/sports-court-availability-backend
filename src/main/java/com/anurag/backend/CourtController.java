package com.anurag.backend;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courts")
public class CourtController {

    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;

    public CourtController(
            CourtRepository courtRepository,
            BookingRepository bookingRepository) {
        this.courtRepository = courtRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<CourtResponse> getAllCourts() {
        LocalDateTime now = LocalDateTime.now();

        Map<Long, Booking> activeBookings = bookingRepository
                .findAllByStatusAndEndTimeAfter("ACTIVE", now)
                .stream()
                .collect(Collectors.toMap(
                        booking -> booking.getCourt().getId(),
                        booking -> booking,
                        (first, duplicate) -> first));

        return courtRepository.findAll()
                .stream()
                .map(court -> {
                    Booking booking = activeBookings.get(court.getId());
                    boolean available = court.isActive() && booking == null;

                    BookingSummary summary = booking == null
                            ? null
                            : new BookingSummary(
                                    booking.getBookingCode(),
                                    booking.getStartTime(),
                                    booking.getEndTime(),
                                    booking.getDurationMinutes());

                    return new CourtResponse(
                            court.getId(),
                            court.getName(),
                            court.getSport(),
                            court.getLocation(),
                            court.isActive(),
                            available,
                            summary);
                })
                .toList();
    }

    public record CourtResponse(
            Long id,
            String name,
            String sport,
            String location,
            boolean active,
            boolean available,
            BookingSummary booking) {
    }

    public record BookingSummary(
            String bookingCode,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer durationMinutes) {
    }
}