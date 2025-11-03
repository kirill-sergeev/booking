package org.example.booking.repository;

import org.example.booking.dto.BookingDateRangeDto;
import org.example.booking.model.Booking;
import org.example.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            SELECT new org.example.booking.dto.BookingDateRangeDto(b.unit.id, b.checkInDate, b.checkOutDate) \
            FROM Booking b \
            WHERE b.status IN ?#{T(org.example.booking.model.BookingStatus).BOOKED_STATUSES}
            """)
    List<BookingDateRangeDto> findActiveBookingRanges();

    List<Booking> findAllByStatusAndExpiresAtBefore(BookingStatus status, Instant now);
}
