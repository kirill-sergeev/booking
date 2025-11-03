package org.example.booking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.booking.dto.BookingCreateRequest;
import org.example.booking.dto.BookingDto;
import org.example.booking.dto.PaymentDto;
import org.example.booking.service.BookingService;
import org.example.booking.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/bookings")
@Tag(name = "Booking Management", description = "APIs for creating, paying, and canceling bookings")
public class BookingController {

    private final BookingService bookingService;
    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Create a new booking",
            description = "Creates a booking in PENDING state. It must be paid within 15 minutes.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Booking created"),
                    @ApiResponse(responseCode = "400", description = "Invalid data or dates"),
                    @ApiResponse(responseCode = "404", description = "Unit or User not found"),
                    @ApiResponse(responseCode = "409", description = "Unit is not available for these dates")
            })
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody BookingCreateRequest createRequest) {
        BookingDto booking = bookingService.createBooking(createRequest);
        return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking details by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Booking found"),
                    @ApiResponse(responseCode = "404", description = "Booking not found")
            })
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    @DeleteMapping("/{bookingId}")
    @Operation(summary = "Cancel a booking",
            description = "Cancels a PENDING or CONFIRMED booking.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Booking cancelled"),
                    @ApiResponse(responseCode = "404", description = "Booking not found")
            })
    public ResponseEntity<BookingDto> cancelBooking(@PathVariable Long bookingId) {
        BookingDto cancelledBooking = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(cancelledBooking);
    }

    @PostMapping("/{bookingId}/pay")
    @Operation(summary = "Emulate payment for a booking",
            description = "Moves a PENDING booking to CONFIRMED state.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payment successful, booking confirmed"),
                    @ApiResponse(responseCode = "404", description = "Booking not found"),
                    @ApiResponse(responseCode = "409", description = "Booking is not pending or has expired")
            })
    public ResponseEntity<PaymentDto> payForBooking(@PathVariable Long bookingId) {
        PaymentDto payment = paymentService.processPayment(bookingId);
        return ResponseEntity.ok(payment);
    }
}
