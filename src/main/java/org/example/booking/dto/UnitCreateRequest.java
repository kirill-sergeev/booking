package org.example.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.booking.model.AccommodationType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data required to create a new accommodation unit")
public class UnitCreateRequest {

    @Min(1)
    @Max(1000)
    @NotNull
    @Schema(description = "Number of rooms in the unit", example = "2")
    private Integer numberOfRooms;

    @NotNull
    @Schema(description = "Type of accommodation", example = "FLAT")
    private AccommodationType accommodationType;

    @Min(0)
    @Max(1000)
    @NotNull
    @Schema(description = "Floor number of the unit", example = "5")
    private Integer floor;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    @Schema(description = "Base cost per night for the unit", example = "150.00")
    private BigDecimal baseCost;

    @Schema(description = "A general description of the unit", example = "Modern flat, fully equipped")
    private String description;
}