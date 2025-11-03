package org.example.booking.service;

import org.example.booking.config.AppConfig;
import org.example.booking.dto.BookingDto;
import org.example.booking.dto.BookingCreateRequest;
import org.example.booking.exception.BookingException;
import org.example.booking.exception.ResourceNotFoundException;
import org.example.booking.model.AccommodationType;
import org.example.booking.model.Booking;
import org.example.booking.model.BookingStatus;
import org.example.booking.model.Unit;
import org.example.booking.model.User;
import org.example.booking.repository.BookingRepository;
import org.example.booking.repository.UnitRepository;
import org.example.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UnitRepository unitRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MarkupService markupService;
    @Mock
    private UnitAvailabilityService unitAvailabilityService;
    @Mock
    private UnitEventService unitEventService;
    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private BookingService bookingService;

    private Unit testUnit;
    private User testUser;
    private BookingCreateRequest testRequest;

    @BeforeEach
    void setUp() {
        testUnit = Unit.builder()
            .id(1L)
            .baseCost(new BigDecimal("100.00"))
            .accommodationType(AccommodationType.FLAT)
            .build();
        
        testUser = User.builder()
            .id(1L)
            .username("John Doe")
            .build();
        
        testRequest = new BookingCreateRequest(
            1L, 1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5)
        );
    }

    @Test
    void whenCreateBooking_withAvailableUnit_shouldSucceed() {
        // Given
        given(appConfig.getCancellationMinutes()).willReturn(15);
        given(unitRepository.findByIdWithLock(1L)).willReturn(Optional.of(testUnit));
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(markupService.calculateTotalUnitCost(any())).willReturn(new BigDecimal("115.00"));
        given(unitAvailabilityService.isUnitAvailable(any(), any(), any())).willReturn(true);

        // Mock the save operation to return the object
        given(bookingRepository.save(any(Booking.class))).willAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(100L); // Simulate saving and getting an ID
            b.setCreatedAt(Instant.now());
            return b;
        });

        // When
        BookingDto result = bookingService.createBooking(testRequest);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(BookingStatus.PENDING, result.getStatus());
        assertEquals(new BigDecimal("115.00"), result.getTotalCost());
        assertNotNull(result.getExpiresAt());

        verify(unitAvailabilityService).addBookedDates(any());
        verify(unitEventService).logEvent(any(), any(), any(), any());
    }

    @Test
    void whenCreateBooking_withUnavailableUnit_shouldThrowBookingException() {
        // Given
        given(unitRepository.findByIdWithLock(1L)).willReturn(Optional.of(testUnit));
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        
        // Simulate an existing booking
        given(unitAvailabilityService.isUnitAvailable(any(), any(), any())).willReturn(false);


        // When & Then
        assertThrows(BookingException.class, () -> bookingService.createBooking(testRequest));
    }

    @Test
    void whenCreateBooking_withMissingUnit_shouldThrowResourceNotFoundException() {
        // Given
        given(unitRepository.findByIdWithLock(1L)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(testRequest));
    }

    @Test
    void whenGetBookingById_withExistingBooking_shouldReturnBookingDto() {
        // Given
        Booking existingBooking = Booking.builder()
            .id(100L)
            .status(BookingStatus.PENDING)
            .unit(testUnit)
            .user(testUser)
            .checkInDate(LocalDate.now().plusDays(1))
            .checkOutDate(LocalDate.now().plusDays(5))
            .totalCost(new BigDecimal("115.00"))
            .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
            .createdAt(Instant.now())
            .build();
        
        given(bookingRepository.findById(100L)).willReturn(Optional.of(existingBooking));

        // When
        BookingDto result = bookingService.getBookingById(100L);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getUnitId());
        assertEquals(1L, result.getUserId());
        assertEquals(BookingStatus.PENDING, result.getStatus());
        assertEquals(new BigDecimal("115.00"), result.getTotalCost());
        assertEquals(existingBooking.getCheckInDate(), result.getCheckInDate());
        assertEquals(existingBooking.getCheckOutDate(), result.getCheckOutDate());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getExpiresAt());
    }

    @Test
    void whenGetBookingById_withNonExistingBooking_shouldThrowResourceNotFoundException() {
        // Given
        given(bookingRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> bookingService.getBookingById(999L));
    }

    @Test
    void whenCreateBooking_withInvalidDates_shouldThrowInvalidRequestException() {
        // Given
        BookingCreateRequest invalidRequest = new BookingCreateRequest(
            1L, 1L, LocalDate.now().plusDays(5), LocalDate.now().plusDays(5) // Same check-in and check-out date
        );

        // When & Then
        assertThrows(Exception.class, () -> bookingService.createBooking(invalidRequest));
    }

    @Test
    void whenCancelBooking_withPendingBooking_shouldSucceed() {
        // Given
        Booking pendingBooking = Booking.builder()
            .id(100L)
            .status(BookingStatus.PENDING)
            .unit(testUnit)
            .user(testUser)
            .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
            .build();
        
        given(bookingRepository.findById(100L)).willReturn(Optional.of(pendingBooking));
        given(bookingRepository.save(any(Booking.class))).willReturn(pendingBooking);

        // When
        BookingDto result = bookingService.cancelBooking(100L);

        // Then
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        assertNull(result.getExpiresAt()); // expiresAt should be cleared
        verify(unitAvailabilityService).removeBookedDates(any());
        verify(unitEventService).logEvent(any(), any(), any(), any());
    }
    
    @Test
    void whenCancelBooking_withAlreadyCancelledBooking_shouldReturnBookingWithoutChanges() {
        // Given
        Booking cancelledBooking = Booking.builder()
            .id(100L)
            .status(BookingStatus.CANCELLED)
            .unit(testUnit)
            .user(testUser)
            .expiresAt(null)
            .build();
        
        given(bookingRepository.findById(100L)).willReturn(Optional.of(cancelledBooking));

        // When
        BookingDto result = bookingService.cancelBooking(100L);

        // Then
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        assertNull(result.getExpiresAt());}
    
    @Test
    void whenCancelBooking_withNonExistingBooking_shouldThrowResourceNotFoundException() {
        // Given
        given(bookingRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> bookingService.cancelBooking(999L));
    }
}
