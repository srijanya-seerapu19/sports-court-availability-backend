package com.anurag.backend;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookingService {

    private static final Set<Integer> ALLOWED_DURATIONS =
            Set.of(15, 30, 45, 60);

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;

    public BookingService(
            BookingRepository bookingRepository,
            CourtRepository courtRepository) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
    }

    @Transactional
    public BookingResponse createBooking(
            Long courtId,
            Integer durationMinutes,
            Student student) {

        if (durationMinutes == null ||
                !ALLOWED_DURATIONS.contains(durationMinutes)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Choose a booking length of 15, 30, 45, or 60 minutes."
            );
        }

        LocalDateTime startTime = LocalDateTime.now();
        bookingRepository.completeExpiredBookings(startTime);

        Court court = courtRepository.findByIdForBooking(courtId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Court not found."
            ));

        if (!court.isActive()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This court is not currently available."
            );
        }

        boolean courtAlreadyBooked =
            bookingRepository.existsByCourt_IdAndStatusAndEndTimeAfter(
                courtId,
                "ACTIVE",
                startTime
            );

        if (courtAlreadyBooked) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This court is already booked."
            );
        }

        boolean studentAlreadyBooked =
            bookingRepository.existsByStudent_IdAndStatusAndEndTimeAfter(
                student.getId(),
                "ACTIVE",
                startTime
            );

        if (studentAlreadyBooked) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "You already have an active booking."
            );
        }

        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
        String bookingCode = "AU-" +
            UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);

        Booking booking = new Booking(
            bookingCode,
            court,
            student,
            startTime,
            endTime,
            durationMinutes
        );

        return toResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(Student student) {
        return bookingRepository
            .findAllByStudent_IdOrderByStartTimeDesc(student.getId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, Student student) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Booking not found."
            ));

        if (!booking.getStudent().getId().equals(student.getId())) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Booking not found."
            );
        }

        if (!"ACTIVE".equals(booking.getStatus()) ||
                !booking.getEndTime().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This booking is no longer active."
            );
        }

        booking.setStatus("CANCELLED");
        return toResponse(bookingRepository.save(booking));
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
            booking.getId(),
            booking.getBookingCode(),
            booking.getCourt().getId(),
            booking.getCourt().getName(),
            booking.getCourt().getSport(),
            booking.getStudent().getEmail(),
            booking.getStartTime(),
            booking.getEndTime(),
            booking.getDurationMinutes(),
            booking.getStatus()
        );
    }

    public record BookingResponse(
        Long id,
        String bookingCode,
        Long courtId,
        String courtName,
        String sport,
        String studentEmail,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer durationMinutes,
        String status
    ) {
    }
}