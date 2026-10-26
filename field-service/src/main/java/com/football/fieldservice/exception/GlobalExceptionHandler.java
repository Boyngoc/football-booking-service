package com.football.fieldservice.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Bat loi tap trung cho field-service.
 *
 * Dinh dang tra ve bam dung docs/api-contract.md muc 0, de booking-service
 * doc duoc message va chuyen tiep cho khach.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 404 - khong tim thay san. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex,
            HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    /** 400 - tham so sai nghiep vu (gio ket thuc <= gio bat dau, ngoai gio mo cua...). */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalid(InvalidRequestException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    /** 400 - body khong qua duoc @Valid. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new TreeMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ", request, errors);
    }

    /** 400 - sai kieu tham so, vi du ?type=SAN_9 hoac start=17h00. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String message = "Tham số '" + ex.getName() + "' có giá trị không hợp lệ: " + ex.getValue();
        return build(HttpStatus.BAD_REQUEST, message, request, null);
    }

    /** 400 - thieu tham so bat buoc tren URL. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Thiếu tham số bắt buộc: " + ex.getParameterName(), request, null);
    }

    /** 400 - JSON gui len sai dinh dang, vi du status = "BAO_TRI". */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Nội dung gửi lên không đọc được hoặc sai định dạng", request, null);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message,
            HttpServletRequest request, Map<String, String> errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequestURI());
        if (errors != null) {
            body.put("errors", errors);
        }
        return ResponseEntity.status(status).body(body);
    }
}
