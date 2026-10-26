package com.football.fieldservice.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Ket qua bao gia mot khung gio thue san.
 *
 * Dinh dang nay da chot trong docs/api-contract.md muc 1.2 - doi la booking-service chet.
 */
public class QuoteResponse {

    private Long fieldId;
    private String fieldName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    /** So gio roi vao khung gio thuong. */
    private double normalHours;

    /** So gio roi vao khung gio vang (17:00-21:00). */
    private double goldenHours;

    /** Tong tien phai tra (VND). booking-service lay thang so nay, khong tinh lai. */
    private long amount;

    public QuoteResponse() {
    }

    public QuoteResponse(Long fieldId, String fieldName, LocalDate date, LocalTime startTime,
            LocalTime endTime, double normalHours, double goldenHours, long amount) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.normalHours = normalHours;
        this.goldenHours = goldenHours;
        this.amount = amount;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public double getNormalHours() {
        return normalHours;
    }

    public double getGoldenHours() {
        return goldenHours;
    }

    public long getAmount() {
        return amount;
    }
}
