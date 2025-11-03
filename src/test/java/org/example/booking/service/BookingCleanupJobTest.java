package org.example.booking.service;

import org.example.booking.model.Booking;
import org.example.booking.model.BookingStatus;
import org.example.booking.model.Unit;
import org.example.booking.model.UnitEventType;
import org.example.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingCleanupJobTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UnitEventService unitEventService;

    @Mock
    private UnitAvailabilityService unitAvailabilityService;

    @InjectMocks
    private BookingCleanupJob bookingCleanupJob;

    private List<Booking> expiredBookings;


    @BeforeEach
    void setUp() {
        Unit testUnit = Unit.builder()
                .id(1L)
                .build();

        // Create a list of expired bookings
        expiredBookings = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Booking booking = Booking.builder()
                    .id((long) i)
                    .status(BookingStatus.PENDING)
                    .expiresAt(Instant.now().minusSeconds(60)) // Expired 1 minute ago
                    .unit(testUnit)
                    .build();
            expiredBookings.add(booking);
        }
    }

    @Test
    void whenExpirePendingBookings_withNoExpiredBookings_shouldDoNothing() {
        // Given
        given(bookingRepository.findAllByStatusAndExpiresAtBefore(
                any(BookingStatus.class), any(Instant.class)))
                .willReturn(Collections.emptyList());

        // When
        bookingCleanupJob.expirePendingBookings();

        // Then
        verify(bookingRepository).findAllByStatusAndExpiresAtBefore(
                any(BookingStatus.class), any(Instant.class));
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(unitEventService, never()).logEvent(any(), any(), any(), any());
        verify(unitAvailabilityService, never()).removeBookedDates(any());
    }

    @Test
    void whenExpirePendingBookings_withExpiredBookings_shouldExpireAll() {
        // Given
        given(bookingRepository.findAllByStatusAndExpiresAtBefore(
                any(BookingStatus.class), any(Instant.class)))
                .willReturn(expiredBookings);

        // When
        bookingCleanupJob.expirePendingBookings();

        // Then
        verify(bookingRepository).findAllByStatusAndExpiresAtBefore(
                any(BookingStatus.class), any(Instant.class));
        
        // Verify each booking was processed
        for (Booking booking : expiredBookings) {
            // Verify status was updated to EXPIRED
            verify(bookingRepository).save(booking);
            
            // Verify event was logged
            verify(unitEventService).logEvent(
                    booking.getUnit(),
                    booking,
                    UnitEventType.BOOKING_EXPIRED,
                    "Booking expired due to non-payment");
            
            // Verify booked dates were removed
            verify(unitAvailabilityService).removeBookedDates(booking);
        }
        
        // Verify the correct number of calls
        verify(bookingRepository, times(expiredBookings.size())).save(any(Booking.class));
        verify(unitEventService, times(expiredBookings.size())).logEvent(any(), any(), any(), any());
        verify(unitAvailabilityService, times(expiredBookings.size())).removeBookedDates(any());
    }
}