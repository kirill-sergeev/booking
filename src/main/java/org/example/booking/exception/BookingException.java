package org.example.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class BookingException extends RuntimeException {

    public BookingException(String message) {
        super(message);
    }
}