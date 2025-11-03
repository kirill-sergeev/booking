package org.example.booking.controller;

import org.example.booking.dto.AvailableUnitsResponse;
import org.example.booking.service.UnitAvailabilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UnitAvailabilityService unitAvailabilityService;

    @Test
    void whenGetAvailableUnitsCount_withValidDates_shouldReturnCount() throws Exception {
        // Given
        LocalDate checkInDate = LocalDate.now().plusDays(1);
        LocalDate checkOutDate = LocalDate.now().plusDays(5);
        
        var response = new AvailableUnitsResponse(42);

        given(unitAvailabilityService.getAvailableUnitsCount(any(LocalDate.class), any(LocalDate.class)))
            .willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/statistics/available-units")
                .param("checkInDate", checkInDate.toString())
                .param("checkOutDate", checkOutDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.availableUnitsCount").value(42));
    }

    @Test
    void whenGetAvailableUnitsCount_withMissingDates_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/statistics/available-units"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void whenGetAvailableUnitsCount_withInvalidDateFormat_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/statistics/available-units")
                .param("checkInDate", "invalid-date")
                .param("checkOutDate", "invalid-date"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void whenGetAvailableUnitsCount_withPastCheckInDate_shouldReturnBadRequest() throws Exception {
        // Given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        LocalDate futureDate = LocalDate.now().plusDays(5);
        
        // When & Then
        mockMvc.perform(get("/api/v1/statistics/available-units")
                .param("checkInDate", pastDate.toString())
                .param("checkOutDate", futureDate.toString()))
            .andExpect(status().isBadRequest());
    }
}