package com.football.fieldservice.exception;

/** Nem ra khi tham so dau vao sai nghiep vu -> GlobalExceptionHandler tra 400. */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
