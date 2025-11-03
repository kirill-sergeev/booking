package org.example.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booking.config.AppConfig;
import org.example.booking.dto.BookingCreateRequest;
import org.example.booking.dto.BookingDto;
import org.example.booking.exception.BookingException;
import org.example.booking.exception.InvalidRequestException;
import org.example.booking.exception.ResourceNotFoundException;
import org.example.booking.model.Booking;
import org.example.booking.model.BookingStatus;
import org.example.booking.model.Unit;
import org.example.booking.model.UnitEventType;
import org.example.booking.model.User;
import org.example.booking.repository.BookingRepository;
import org.example.booking.repository.UnitRepository;
import org.example.booking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final AppConfig appConfig;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final MarkupService markupService;
    private final UnitEventService unitEventService;
    private final UnitAvailabilityService unitAvailabilityService;

    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long bookingId) {
        return toDto(findBookingById(bookingId));
    }

    @Transactional
    public BookingDto createBooking(BookingCreateRequest request) {
        log.info("Attempting to create booking for unit: {}", request.getUnitId());

        validateBookingRequest(request);

        // Find and lock the unit to prevent concurrent bookings
        Unit unit = unitRepository.findByIdWithLock(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + request.getUnitId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        boolean unitAvailable = unitAvailabilityService.isUnitAvailable(unit.getId(), request.getCheckInDate(), request.getCheckOutDate());

        if (!unitAvailable) {
            log.warn("Booking conflict for unit {}: Dates {} to {} are not available",
                    unit.getId(), request.getCheckInDate(), request.getCheckOutDate());
            throw new BookingException("The selected unit is not available for the chosen dates.");
        }

        BigDecimal totalCost = markupService.calculateTotalUnitCost(unit.getBaseCost());
        Instant expiresAt = Instant.now().plus(appConfig.getCancellationMinutes(), ChronoUnit.MINUTES);

        Booking booking = Booking.builder()
                .unit(unit)
                .user(user)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .status(BookingStatus.PENDING)
                .totalCost(totalCost)
                .expiresAt(expiresAt)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        unitAvailabilityService.addBookedDates(savedBooking);
        unitEventService.logEvent(unit, savedBooking, UnitEventType.BOOKING_CREATED, "Booking created in PENDING state");

        log.info("Booking {} created for unit {}", savedBooking.getId(), unit.getId());
        return toDto(savedBooking);
    }

    @Transactional
    public BookingDto cancelBooking(Long bookingId) {
        log.debug("Attempting to cancel booking: {}", bookingId);

        Booking booking = findBookingById(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.EXPIRED) {
            log.info("Booking {} is already in terminal state {}", bookingId, booking.getStatus());
            return toDto(booking);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setExpiresAt(null);

        Booking savedBooking = bookingRepository.save(booking);
        unitAvailabilityService.removeBookedDates(savedBooking);
        unitEventService.logEvent(booking.getUnit(), savedBooking, UnitEventType.BOOKING_CANCELLED, "Booking cancelled by user");

        log.info("Booking {} cancelled successfully", savedBooking.getId());
        return toDto(savedBooking);
    }

    private void validateBookingRequest(BookingCreateRequest request) {
        if (request.getCheckInDate().isAfter(request.getCheckOutDate())
                || request.getCheckInDate().isEqual(request.getCheckOutDate())) {
            throw new InvalidRequestException("Check-out date must be after check-in date");
        }
    }

    protected Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
    }

    private BookingDto toDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getUnit().getId(),
                booking.getUser().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getStatus(),
                booking.getTotalCost(),
                booking.getCreatedAt(),
                booking.getExpiresAt()
        );
    }
}
