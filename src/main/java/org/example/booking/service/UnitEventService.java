package org.example.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booking.model.Booking;
import org.example.booking.model.Unit;
import org.example.booking.model.UnitEvent;
import org.example.booking.model.UnitEventType;
import org.example.booking.repository.UnitEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class UnitEventService {

    private final UnitEventRepository unitEventRepository;

    @Transactional
    public void logEvent(Unit unit, Booking booking, UnitEventType eventType, String details) {
        try {
            UnitEvent event = UnitEvent.builder()
                    .unit(unit)
                    .booking(booking)
                    .eventType(eventType)
                    .details(details)
                    .build();
            unitEventRepository.save(event);
        } catch (Exception e) {
            // We log the error but do not throw, as logging failure should not fail the main business operation.
            log.error("Failed to log event: type={}, unitId={}, bookingId={}",
                    eventType,
                    unit,
                    booking != null ? booking.getId() : "null",
                    e);
        }
    }
}
