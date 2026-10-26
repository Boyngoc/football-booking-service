package com.football.fieldservice.model;

/**
 * Loai san theo so nguoi moi ben.
 */
public enum FieldType {

    SAN_5("Sân 5 người"),
    SAN_7("Sân 7 người"),
    SAN_11("Sân 11 người");

    private final String moTa;

    FieldType(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }
}
