package com.football.fieldservice.model;

/**
 * Trang thai khai thac cua mot san.
 *
 * ACTIVE      : dang nhan dat lich binh thuong
 * MAINTENANCE : dang bao tri - booking-service phai tra 409 khi khach co dat
 */
public enum FieldStatus {
    ACTIVE,
    MAINTENANCE
}
