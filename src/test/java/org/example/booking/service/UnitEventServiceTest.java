package org.example.booking.service;

import org.example.booking.model.Booking;
import org.example.booking.model.Unit;
import org.example.booking.model.UnitEvent;
import org.example.booking.model.UnitEventType;
import org.example.booking.repository.UnitEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UnitEventServiceTest {

    @Mock
    private UnitEventRepository unitEventRepository;

    @InjectMocks
    private UnitEventService unitEventService;

    @Captor
    private ArgumentCaptor<UnitEvent> unitEventCaptor;

    private Unit testUnit;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testUnit = Unit.builder()
                .id(1L)
                .baseCost(new BigDecimal("100.00"))
                .build();

        testBooking = Booking.builder()
                .id(1L)
                .unit(testUnit)
                .build();
    }

    @Test
    void whenLogEvent_withValidData_shouldSaveEvent() {
        // Given
        UnitEventType eventType = UnitEventType.BOOKING_CONFIRMED;
        String details = "Test event details";

        // When
        unitEventService.logEvent(testUnit, testBooking, eventType, details);

        // Then
        verify(unitEventRepository).save(unitEventCaptor.capture());
        
        UnitEvent savedEvent = unitEventCaptor.getValue();
        assertEquals(testUnit, savedEvent.getUnit());
        assertEquals(testBooking, savedEvent.getBooking());
        assertEquals(eventType, savedEvent.getEventType());
        assertEquals(details, savedEvent.getDetails());
    }

    @Test
    void whenLogEvent_withNullBooking_shouldSaveEventWithNullBooking() {
        // Given
        UnitEventType eventType = UnitEventType.UNIT_CREATED;
        String details = "Unit created via API";

        // When
        unitEventService.logEvent(testUnit, null, eventType, details);

        // Then
        verify(unitEventRepository).save(unitEventCaptor.capture());
        
        UnitEvent savedEvent = unitEventCaptor.getValue();
        assertEquals(testUnit, savedEvent.getUnit());
        assertNull(savedEvent.getBooking());
        assertEquals(eventType, savedEvent.getEventType());
        assertEquals(details, savedEvent.getDetails());
    }

    @Test
    void whenLogEvent_withRepositoryException_shouldCatchAndNotPropagate() {
        // Given
        doThrow(new RuntimeException("Database error")).when(unitEventRepository).save(any(UnitEvent.class));

        // When
        unitEventService.logEvent(testUnit, testBooking, UnitEventType.BOOKING_CONFIRMED, "Test event");

        // Then
        // No exception should be thrown
        verify(unitEventRepository).save(any(UnitEvent.class));
    }
}