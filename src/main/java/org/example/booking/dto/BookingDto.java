package org.example.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.booking.model.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed information about a booking")
public class BookingDto {

    @Schema(description = "Unique identifier for the booking", example = "101")
    private Long id;

    @Schema(description = "ID of the booked unit", example = "1")
    private Long unitId;

    @Schema(description = "ID of the user who booked", example = "1")
    private Long userId;

    @Schema(description = "Check-in date", example = "2025-12-20")
    private LocalDate checkInDate;

    @Schema(description = "Check-out date", example = "2025-12-25")
    private LocalDate checkOutDate;

    @Schema(description = "Current status of the booking", example = "PENDING")
    private BookingStatus status;

    @Schema(description = "Total cost including system markup", example = "172.50")
    private BigDecimal totalCost;

    @Schema(description = "Timestamp when the booking was created")
    private Instant createdAt;

    @Schema(description = "Timestamp when the pending booking will expire if not paid")
    private Instant expiresAt;
}