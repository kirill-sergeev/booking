package org.example.booking.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.booking.dto.PagedResponse;
import org.example.booking.dto.UnitCreateRequest;
import org.example.booking.dto.UnitDto;
import org.example.booking.dto.UnitSearchRequest;
import org.example.booking.service.UnitService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/units")
@Tag(name = "Unit Management", description = "APIs for managing and searching accommodation units")
public class UnitController {

    private final UnitService unitService;

    @PostMapping
    @Operation(summary = "Add a new accommodation unit",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Unit created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            })
    public ResponseEntity<UnitDto> addUnit(@Valid @RequestBody UnitCreateRequest createDto) {
        UnitDto newUnit = unitService.createUnit(createDto);
        return new ResponseEntity<>(newUnit, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a unit by its ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Unit found"),
                    @ApiResponse(responseCode = "404", description = "Unit not found")
            })
    public ResponseEntity<UnitDto> getUnitById(@PathVariable Long id) {
        return ResponseEntity.ok(unitService.getUnitById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search for available units",
            description = "Finds units based on criteria and availability. " +
                    "Supports sorting (e.g., sort=baseCost,asc) and " +
                    "pagination (e.g., page=0&size=10).")
    public ResponseEntity<PagedResponse<UnitDto>> searchUnits(
            @Valid @ParameterObject UnitSearchRequest criteria,
            @ParameterObject @PageableDefault(sort = "baseCost") Pageable pageable) {
        Slice<UnitDto> results = unitService.findAvailableUnits(criteria, pageable);
        return ResponseEntity.ok(PagedResponse.fromSlice(results));
    }
}
