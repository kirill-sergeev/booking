package org.example.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Information about a successful payment")
public class PaymentDto {

    @Schema(description = "Unique identifier for the payment", example = "501")
    private Long id;

    @Schema(description = "ID of the associated booking", example = "101")
    private Long bookingId;

    @Schema(description = "Payment status", example = "SUCCESSFUL")
    private String status;

    @Schema(description = "Amount paid", example = "172.50")
    private BigDecimal amount;

    @Schema(description = "Timestamp of payment")
    private Instant paidAt;
}