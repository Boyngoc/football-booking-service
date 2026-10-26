package com.football.fieldservice.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.football.fieldservice.dto.QuoteResponse;
import com.football.fieldservice.exception.ResourceNotFoundException;
import com.football.fieldservice.model.Field;
import com.football.fieldservice.model.FieldStatus;
import com.football.fieldservice.model.FieldType;
import com.football.fieldservice.repository.FieldRepository;

/**
 * Tang nghiep vu cua field-service.
 */
@Service
public class FieldService {

    private final FieldRepository fieldRepository;
    private final PricingService pricingService;

    public FieldService(FieldRepository fieldRepository, PricingService pricingService) {
        this.fieldRepository = fieldRepository;
        this.pricingService = pricingService;
    }

    @Transactional(readOnly = true)
    public List<Field> danhSach(FieldType type) {
        return type == null
                ? fieldRepository.findAllByOrderByIdAsc()
                : fieldRepository.findByTypeOrderByIdAsc(type);
    }

    @Transactional(readOnly = true)
    public Field timTheoMa(Long id) {
        return fieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân có mã " + id));
    }

    @Transactional(readOnly = true)
    public QuoteResponse baoGia(Long id, LocalDate date, LocalTime start, LocalTime end) {
        return pricingService.quote(timTheoMa(id), date, start, end);
    }

    /** Chu san bat / tat bao tri. Chi vai tro OWNER duoc goi (Hoang chan o SecurityConfig). */
    @Transactional
    public Field doiTrangThai(Long id, FieldStatus trangThaiMoi) {
        Field field = timTheoMa(id);
        field.setStatus(trangThaiMoi);
        return fieldRepository.save(field);
    }
}
