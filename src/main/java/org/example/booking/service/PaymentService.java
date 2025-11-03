package org.example.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booking.dto.PaymentDto;
import org.example.booking.exception.BookingException;
import org.example.booking.model.Booking;
import org.example.booking.model.BookingStatus;
import org.example.booking.model.Payment;
import org.example.booking.model.PaymentStatus;
import org.example.booking.model.UnitEventType;
import org.example.booking.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final UnitEventService unitEventService;

    @Transactional
    public PaymentDto processPayment(Long bookingId) {
        log.info("Attempting to process payment for booking: {}", bookingId);

        Booking booking = bookingService.findBookingById(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn("Payment failed: Booking {} is not in PENDING state (is {})", bookingId, booking.getStatus());
            throw new BookingException("Payment failed: Booking is not in PENDING state.");
        }

        if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Payment failed: Booking {} has expired at {}", bookingId, booking.getExpiresAt());
            throw new BookingException("Payment failed: Booking has expired.");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setExpiresAt(null);

        Payment payment = Payment.builder()
                .booking(booking)
                .status(PaymentStatus.SUCCESSFUL)
                .amount(booking.getTotalCost())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        unitEventService.logEvent(booking.getUnit(), booking, UnitEventType.BOOKING_CONFIRMED, "Booking payment successful");

        log.info("Payment successful for booking {}. Status set to CONFIRMED.", bookingId);
        return toDto(savedPayment);
    }

    private PaymentDto toDto(Payment payment) {
        return new PaymentDto(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getPaidAt()
        );
    }
}
