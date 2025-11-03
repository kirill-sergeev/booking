package org.example.booking.model;

import java.util.EnumSet;
import java.util.Set;

public enum BookingStatus {

    PENDING,    // Initial state, awaiting payment
    CONFIRMED,  // Paid and confirmed
    CANCELLED,  // Canceled by the user
    EXPIRED;     // Auto-cancelled due to non-payment

    public static final Set<BookingStatus> BOOKED_STATUSES = EnumSet.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
}