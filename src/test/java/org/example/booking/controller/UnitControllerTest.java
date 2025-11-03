package org.example.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.booking.dto.UnitCreateRequest;
import org.example.booking.dto.UnitDto;
import org.example.booking.dto.UnitSearchRequest;
import org.example.booking.model.AccommodationType;
import org.example.booking.service.UnitAvailabilityService;
import org.example.booking.service.UnitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UnitController.class)
class UnitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UnitService unitService;

    @MockitoBean
    private UnitAvailabilityService unitAvailabilityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenAddUnit_withValidData_shouldReturnCreated() throws Exception {
        var requestDto = new UnitCreateRequest(
            2, AccommodationType.FLAT, 3, new BigDecimal("100.00"), "Test flat"
        );
        
        var responseDto = new UnitDto(
            1L, 2, AccommodationType.FLAT, 3, new BigDecimal("100.00"), "Test flat", Instant.now()
        );

        given(unitService.createUnit(any(UnitCreateRequest.class))).willReturn(responseDto);

        mockMvc.perform(post("/api/v1/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.numberOfRooms").value(2))
            .andExpect(jsonPath("$.description").value("Test flat"));
    }

    @Test
    void whenAddUnit_withInvalidData_shouldReturnBadRequest() throws Exception {
        // numberOfRooms is null, which violates @NotNull
        var requestDto = new UnitCreateRequest(
            null, AccommodationType.FLAT, 3, new BigDecimal("100.00"), "Test flat"
        );

        mockMvc.perform(post("/api/v1/units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void whenGetUnitById_withExistingId_shouldReturnUnit() throws Exception {
        var responseDto = new UnitDto(
            1L, 2, AccommodationType.FLAT, 3, new BigDecimal("100.00"), "Test flat", Instant.now()
        );

        given(unitService.getUnitById(1L)).willReturn(responseDto);

        mockMvc.perform(get("/api/v1/units/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L));
    }
    
    @Test
    void whenSearchUnits_withValidCriteria_shouldReturnPagedResponse() throws Exception {
        // Given
        LocalDate checkInDate = LocalDate.now().plusDays(1);
        LocalDate checkOutDate = LocalDate.now().plusDays(5);
        
        var unit1 = new UnitDto(
            1L, 2, AccommodationType.FLAT, 3, new BigDecimal("100.00"), "Test flat 1", Instant.now()
        );
        
        var unit2 = new UnitDto(
            2L, 3, AccommodationType.HOME, 0, new BigDecimal("150.00"), "Test home", Instant.now()
        );
        
        // Create a slice of units
        List<UnitDto> units = List.of(unit1, unit2);
        Pageable pageable = PageRequest.of(0, 10);
        SliceImpl<UnitDto> slice = new SliceImpl<>(units, pageable, false);
        
        given(unitService.findAvailableUnits(any(UnitSearchRequest.class), any(Pageable.class)))
            .willReturn(slice);
        
        // When & Then
        mockMvc.perform(get("/api/v1/units/search")
                .param("checkInDate", checkInDate.toString())
                .param("checkOutDate", checkOutDate.toString())
                .param("accommodationType", "FLAT")
                .param("minCost", "50.00")
                .param("maxCost", "200.00")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[1].id").value(2))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.hasNext").value(false))
            .andExpect(jsonPath("$.numberOfElements").value(2));
    }
    
    @Test
    void whenSearchUnits_withInvalidCriteria_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/units/search"))
            .andExpect(status().isBadRequest());
    }
}
