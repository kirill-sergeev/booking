package org.example.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.booking.model.AccommodationType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Search criteria for finding available units")
public class UnitSearchRequest {

    @Min(1)
    @Max(1000)
    @Schema(description = "Filter by exact number of rooms", example = "2")
    private Integer numberOfRooms;

    @Schema(description = "Filter by accommodation type", example = "FLAT")
    private AccommodationType accommodationType;

    @Min(0)
    @Max(1000)
    @Schema(description = "Filter by exact floor number", example = "5")
    private Integer floor;

    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    @Schema(description = "Filter by minimum base cost", example = "100.00")
    private BigDecimal minCost;

    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    @Schema(description = "Filter by maximum base cost", example = "200.00")
    private BigDecimal maxCost;

    @NotNull
    @FutureOrPresent
    @Schema(description = "Desired check-in date", example = "2025-12-20")
    private LocalDate checkInDate;

    @NotNull
    @Future
    @Schema(description = "Desired check-out date", example = "2025-12-25")
    private LocalDate checkOutDate;
}