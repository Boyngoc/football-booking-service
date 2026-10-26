package com.football.fieldservice.exception;

/** Nem ra khi khong tim thay tai nguyen -> GlobalExceptionHandler tra 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
