package org.example.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(description = "Status code", example = "404")
    private int status;

    @Schema(description = "Error message", example = "Unit not found")
    private String message;

    @Schema(description = "Timestamp of the error")
    private Instant timestamp;
}
