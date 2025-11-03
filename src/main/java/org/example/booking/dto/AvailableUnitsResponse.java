package org.example.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Statistics on available units")
public class AvailableUnitsResponse {

    @Schema(description = "Total number of units currently available for booking for the specified dates", example = "95")
    private int availableUnitsCount;
}
