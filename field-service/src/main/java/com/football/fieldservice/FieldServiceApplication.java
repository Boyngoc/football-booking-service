package com.football.fieldservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * field-service - cong 8082 - chu so huu: HUNG
 *
 * Can lam tiep (xem docs/PHAN_CONG_KT1.md muc 3):
 *   model/Field.java, model/FieldType.java    : SAN_5 | SAN_7 | SAN_11
 *   model/FieldStatus.java                    : ACTIVE | MAINTENANCE
 *   repository/FieldRepository.java
 *   service/PricingService.java               : tinh tien gio thuong / gio vang (17:00-21:00, he so 1.5)
 *   controller/FieldController.java           : GET /api/fields, /api/fields/{id},
 *                                               GET /api/fields/{id}/quote, PUT /api/fields/{id}/status
 */
@SpringBootApplication(scanBasePackages = { "com.football.fieldservice", "com.football.common" })
public class FieldServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FieldServiceApplication.class, args);
    }
}
