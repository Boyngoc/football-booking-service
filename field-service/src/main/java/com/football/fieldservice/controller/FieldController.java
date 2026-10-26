package com.football.fieldservice.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.football.fieldservice.dto.QuoteResponse;
import com.football.fieldservice.dto.UpdateStatusRequest;
import com.football.fieldservice.model.Field;
import com.football.fieldservice.model.FieldType;
import com.football.fieldservice.service.FieldService;

import jakarta.validation.Valid;

/**
 * REST API cua field-service - cong 8082.
 *
 * Endpoint khai bao dung theo docs/api-contract.md muc 1.
 * booking-service la nguoi tieu thu chinh cua hai endpoint dau.
 */
@RestController
@RequestMapping("/api/fields")
public class FieldController {

    private final FieldService fieldService;

    public FieldController(FieldService fieldService) {
        this.fieldService = fieldService;
    }

    /**
     * Danh sach san, co the loc theo loai.
     * GET /api/fields            -> 200
     * GET /api/fields?type=SAN_7 -> 200
     */
    @GetMapping
    public List<Field> danhSach(@RequestParam(required = false) FieldType type) {
        return fieldService.danhSach(type);
    }

    /**
     * Chi tiet mot san - booking-service goi de kiem tra san co ton tai
     * va status co phai ACTIVE khong.
     * GET /api/fields/{id} -> 200 / 404
     */
    @GetMapping("/{id}")
    public Field chiTiet(@PathVariable Long id) {
        return fieldService.timTheoMa(id);
    }

    /**
     * Bao gia mot khung gio - booking-service goi de lay so tien, KHONG tu tinh.
     * GET /api/fields/{id}/quote?date=2026-11-02&start=17:00&end=18:30 -> 200 / 400 / 404
     */
    @GetMapping("/{id}/quote")
    public QuoteResponse baoGia(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime start,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime end) {
        return fieldService.baoGia(id, date, start, end);
    }

    /**
     * Chu san bat / tat bao tri.
     * PUT /api/fields/{id}/status  body { "status": "MAINTENANCE" } -> 200 / 400 / 404
     * Khi Hoang bat JWT: chi vai tro OWNER goi duoc, CUSTOMER se nhan 403.
     */
    @PutMapping("/{id}/status")
    public Field doiTrangThai(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return fieldService.doiTrangThai(id, request.getStatus());
    }
}
