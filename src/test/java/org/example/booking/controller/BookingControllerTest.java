package org.example.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.booking.dto.BookingCreateRequest;
import org.example.booking.dto.BookingDto;
import org.example.booking.dto.PaymentDto;
import org.example.booking.model.BookingStatus;
import org.example.booking.service.BookingService;
import org.example.booking.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenCreateBooking_withValidData_shouldReturnCreated() throws Exception {
        LocalDate checkInDate = LocalDate.now().plusDays(1);
        LocalDate checkOutDate = LocalDate.now().plusDays(5);

        var requestDto = new BookingCreateRequest(
                1L, 1L, checkInDate, checkOutDate
        );

        var responseDto = new BookingDto(
                1L, 1L, 1L, checkInDate, checkOutDate,
                BookingStatus.PENDING, new BigDecimal("150.00"),
                Instant.now(), Instant.now().plusSeconds(900)
        );

        given(bookingService.createBooking(any(BookingCreateRequest.class))).willReturn(responseDto);

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.unitId").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalCost").value(150.00));
    }

    @Test
    void whenCreateBooking_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Missing required fields
        var requestDto = new BookingCreateRequest(
                null, null, null, null
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGetBookingById_withExistingId_shouldReturnBooking() throws Exception {
        LocalDate checkInDate = LocalDate.now().plusDays(1);
        LocalDate checkOutDate = LocalDate.now().plusDays(5);

        var responseDto = new BookingDto(
                1L, 1L, 1L, checkInDate, checkOutDate,
                BookingStatus.PENDING, new BigDecimal("150.00"),
                Instant.now(), Instant.now().plusSeconds(900)
        );

        given(bookingService.getBookingById(1L)).willReturn(responseDto);

        mockMvc.perform(get("/api/v1/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void whenCancelBooking_withExistingId_shouldReturnCancelledBooking() throws Exception {
        LocalDate checkInDate = LocalDate.now().plusDays(1);
        LocalDate checkOutDate = LocalDate.now().plusDays(5);

        var responseDto = new BookingDto(
                1L, 1L, 1L, checkInDate, checkOutDate,
                BookingStatus.CANCELLED, new BigDecimal("150.00"),
                Instant.now(), null
        );

        given(bookingService.cancelBooking(1L)).willReturn(responseDto);

        mockMvc.perform(delete("/api/v1/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void whenPayForBooking_withExistingId_shouldReturnPayment() throws Exception {
        var paymentDto = new PaymentDto(1L, 1L, "SUCCESSFUL", new BigDecimal("150.00"), Instant.now());

        given(paymentService.processPayment(anyLong())).willReturn(paymentDto);

        mockMvc.perform(post("/api/v1/bookings/1/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bookingId").value(1L))
                .andExpect(jsonPath("$.status").value("SUCCESSFUL"))
                .andExpect(jsonPath("$.amount").value(150.00));
    }
}