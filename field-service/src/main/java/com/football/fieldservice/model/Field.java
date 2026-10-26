package com.football.fieldservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * San bong - du lieu THUOC QUYEN SO HUU cua field-service.
 *
 * Dich vu khac muon biet gia san thi phai goi API /api/fields/{id}/quote,
 * tuyet doi khong doc truc tiep bang nay.
 */
@Entity
@Table(name = "fields")
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FieldType type;

    /** Gia thue mot gio trong khung gio thuong (VND). */
    @Column(name = "price_per_hour", nullable = false)
    private Long pricePerHour;

    /** Gia thue mot gio trong khung gio vang (VND). */
    @Column(name = "golden_price_per_hour", nullable = false)
    private Long goldenPricePerHour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FieldStatus status;

    public Field() {
    }

    public Field(String name, FieldType type, Long pricePerHour, Long goldenPricePerHour, FieldStatus status) {
        this.name = name;
        this.type = type;
        this.pricePerHour = pricePerHour;
        this.goldenPricePerHour = goldenPricePerHour;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public Long getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(Long pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public Long getGoldenPricePerHour() {
        return goldenPricePerHour;
    }

    public void setGoldenPricePerHour(Long goldenPricePerHour) {
        this.goldenPricePerHour = goldenPricePerHour;
    }

    public FieldStatus getStatus() {
        return status;
    }

    public void setStatus(FieldStatus status) {
        this.status = status;
    }
}
