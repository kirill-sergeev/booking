package org.example.booking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.booking.dto.AvailableUnitsRequest;
import org.example.booking.dto.AvailableUnitsResponse;
import org.example.booking.service.UnitAvailabilityService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/statistics")
@Tag(name = "Statistics", description = "APIs for application statistics")
public class StatisticsController {

    private final UnitAvailabilityService unitAvailabilityService;

    @GetMapping("/available-units")
    @Operation(summary = "Get available unit count for a date range",
            description = "Checks availability based on booked dates.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Availability calculated"),
                    @ApiResponse(responseCode = "400", description = "Invalid date range or missing parameters")
            })
    public ResponseEntity<AvailableUnitsResponse> getAvailableUnitsCount(
            @Valid @ParameterObject AvailableUnitsRequest request) {
        return ResponseEntity.ok(unitAvailabilityService.getAvailableUnitsCount(
                request.getCheckInDate(), request.getCheckOutDate()));
    }
}
