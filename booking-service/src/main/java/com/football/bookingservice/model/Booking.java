package com.football.bookingservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class Booking {

    private Long id;

    @NotBlank(message = "Ten khach hang khong duoc de trong")
    private String customerName;

    @NotBlank(message = "So dien thoai khong duoc de trong")
    private String phone;

    @NotBlank(message = "Ten san khong duoc de trong")
    private String fieldName;

    @NotNull(message = "Ngay dat san khong duoc de trong")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;

    @NotNull(message = "Gio bat dau khong duoc de trong")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "Gio ket thuc khong duoc de trong")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotNull(message = "Gia thue khong duoc de trong")
    @Positive(message = "Gia thue phai lon hon 0")
    private Double price;

    private BookingStatus status;

    public Booking() {
    }

    public Booking(Long id, String customerName, String phone, String fieldName,
            LocalDate bookingDate, LocalTime startTime, LocalTime endTime,
            Double price, BookingStatus status) {
        this.id = id;
        this.customerName = customerName;
        this.phone = phone;
        this.fieldName = fieldName;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}