package org.example.booking.controller;

import org.example.booking.dto.ErrorResponse;
import org.example.booking.exception.BookingException;
import org.example.booking.exception.InvalidRequestException;
import org.example.booking.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void whenResourceNotFound_shouldReturnNotFound() {
        // Given
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");
        
        // When
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void whenBookingException_shouldReturnConflict() {
        // Given
        BookingException ex = new BookingException("Booking conflict");
        
        // When
        ResponseEntity<ErrorResponse> response = handler.handleBookingException(ex);
        
        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Booking conflict", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void whenInvalidRequest_shouldReturnBadRequest() {
        // Given
        InvalidRequestException ex = new InvalidRequestException("Invalid request");
        
        // When
        ResponseEntity<ErrorResponse> response = handler.handleInvalidRequest(ex);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Invalid request", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void whenGenericException_shouldReturnInternalServerError() {
        // Given
        Exception ex = new RuntimeException("Some unexpected error");
        
        // When
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }
}