package com.anurag.backend;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingService.BookingResponse createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal Student student) {
        return bookingService.createBooking(
            request.courtId(),
            request.durationMinutes(),
            student
        );
    }

    @GetMapping("/mine")
    public List<BookingService.BookingResponse> getMyBookings(
            @AuthenticationPrincipal Student student) {
        return bookingService.getMyBookings(student);
    }

    @DeleteMapping("/{bookingId}")
    public BookingService.BookingResponse cancelBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal Student student) {
        return bookingService.cancelBooking(bookingId, student);
    }

    public record CreateBookingRequest(
        @NotNull Long courtId,
        @NotNull @Min(15) @Max(60) Integer durationMinutes
    ) {
    }
}