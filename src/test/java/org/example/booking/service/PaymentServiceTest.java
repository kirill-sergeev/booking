package org.example.booking.service;

import org.example.booking.dto.PaymentDto;
import org.example.booking.exception.BookingException;
import org.example.booking.model.Booking;
import org.example.booking.model.BookingStatus;
import org.example.booking.model.Payment;
import org.example.booking.model.PaymentStatus;
import org.example.booking.model.Unit;
import org.example.booking.model.UnitEventType;
import org.example.booking.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingService bookingService;

    @Mock
    private UnitEventService unitEventService;

    @InjectMocks
    private PaymentService paymentService;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    private Booking pendingBooking;
    private Booking confirmedBooking;
    private Booking expiredBooking;
    private Unit testUnit;

    @BeforeEach
    void setUp() {
        testUnit = Unit.builder()
                .id(1L)
                .build();

        // Create a pending booking (valid for payment)
        pendingBooking = Booking.builder()
                .id(1L)
                .status(BookingStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(300)) // Expires in 5 minutes
                .totalCost(new BigDecimal("100.00"))
                .unit(testUnit)
                .build();

        // Create a confirmed booking (invalid for payment)
        confirmedBooking = Booking.builder()
                .id(2L)
                .status(BookingStatus.CONFIRMED)
                .expiresAt(null)
                .totalCost(new BigDecimal("100.00"))
                .unit(testUnit)
                .build();

        // Create an expired booking (invalid for payment)
        expiredBooking = Booking.builder()
                .id(3L)
                .status(BookingStatus.PENDING)
                .expiresAt(Instant.now().minusSeconds(60)) // Expired 1 minute ago
                .totalCost(new BigDecimal("100.00"))
                .unit(testUnit)
                .build();
    }

    @Test
    void whenProcessPayment_withValidPendingBooking_shouldSucceed() {
        // Given
        given(bookingService.findBookingById(1L)).willReturn(pendingBooking);
        given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(100L);
            payment.setPaidAt(Instant.now());
            return payment;
        });

        // When
        PaymentDto result = paymentService.processPayment(1L);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getBookingId());
        assertEquals(PaymentStatus.SUCCESSFUL.name(), result.getStatus());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertNotNull(result.getPaidAt());

        // Verify booking was updated
        assertEquals(BookingStatus.CONFIRMED, pendingBooking.getStatus());
        assertNull(pendingBooking.getExpiresAt());

        // Verify payment was saved
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        assertEquals(pendingBooking, savedPayment.getBooking());
        assertEquals(PaymentStatus.SUCCESSFUL, savedPayment.getStatus());
        assertEquals(pendingBooking.getTotalCost(), savedPayment.getAmount());

        // Verify event was logged
        verify(unitEventService).logEvent(
                testUnit,
                pendingBooking,
                UnitEventType.BOOKING_CONFIRMED,
                "Booking payment successful");
    }

    @Test
    void whenProcessPayment_withNonPendingBooking_shouldThrowException() {
        // Given
        given(bookingService.findBookingById(2L)).willReturn(confirmedBooking);

        // When & Then
        BookingException exception = assertThrows(BookingException.class,
                () -> paymentService.processPayment(2L));
        assertEquals("Payment failed: Booking is not in PENDING state.", exception.getMessage());
    }

    @Test
    void whenProcessPayment_withExpiredBooking_shouldThrowException() {
        // Given
        given(bookingService.findBookingById(3L)).willReturn(expiredBooking);

        // When & Then
        BookingException exception = assertThrows(BookingException.class,
                () -> paymentService.processPayment(3L));
        assertEquals("Payment failed: Booking has expired.", exception.getMessage());
    }
}