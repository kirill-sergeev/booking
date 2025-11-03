package org.example.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.example.booking.model.Booking;
import org.example.booking.model.BookingStatus;
import org.example.booking.model.UnitEventType;
import org.example.booking.repository.BookingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCleanupJob {

    private static final String CRON_EVERY_MINUTE = "0 * * * * *";

    private final BookingRepository bookingRepository;
    private final UnitEventService unitEventService;
    private final UnitAvailabilityService unitAvailabilityService;

    @Transactional
    @Scheduled(cron = CRON_EVERY_MINUTE)
    @SchedulerLock(name = "cancel-expired-bookings-lock")
    public void expirePendingBookings() {
        log.debug("Running scheduled task to cancel expired bookings...");

        List<Booking> expiredBookings = bookingRepository.findAllByStatusAndExpiresAtBefore(
                BookingStatus.PENDING,
                Instant.now()
        );

        if (expiredBookings.isEmpty()) {
            log.debug("No expired bookings found.");
            return;
        }

        log.info("Found {} expired pending bookings. Cancelling...", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.EXPIRED);
            booking.setExpiresAt(null);

            unitEventService.logEvent(booking.getUnit(), booking, UnitEventType.BOOKING_EXPIRED, "Booking expired due to non-payment");
            bookingRepository.save(booking);
            unitAvailabilityService.removeBookedDates(booking);
        }

        log.info("Cancelled {} expired bookings.", expiredBookings.size());
    }
}
