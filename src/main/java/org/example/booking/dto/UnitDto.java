package org.example.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.booking.model.AccommodationType;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed information about an accommodation unit")
public class UnitDto {

    @Schema(description = "Unique identifier for the unit", example = "1")
    private Long id;

    @Schema(description = "Number of rooms in the unit", example = "2")
    private int numberOfRooms;

    @Schema(description = "Type of accommodation", example = "FLAT")
    private AccommodationType accommodationType;

    @Schema(description = "Floor number of the unit", example = "5")
    private int floor;

    @Schema(description = "Total cost per night for the unit", example = "150.00")
    private BigDecimal totalCost;

    @Schema(description = "A general description of the unit", example = "Modern flat, fully equipped")
    private String description;

    @Schema(description = "Timestamp of unit creation")
    private Instant createdAt;
}
