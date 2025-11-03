package org.example.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class BookingDateRangeDto {

    private Long unitId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}