package org.example.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data required to create a new booking")
public class BookingCreateRequest {

    @NotNull
    @Schema(description = "The ID of the unit to book", example = "1")
    private Long unitId;

    @NotNull
    @Schema(description = "The ID of the user making the booking", example = "1")
    private Long userId;

    @NotNull
    @FutureOrPresent
    @Schema(description = "Desired check-in date", example = "2025-12-20")
    private LocalDate checkInDate;

    @NotNull
    @Future
    @Schema(description = "Desired check-out date", example = "2025-12-25")
    private LocalDate checkOutDate;
}
