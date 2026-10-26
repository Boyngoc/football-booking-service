package com.football.fieldservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.football.fieldservice.model.Field;
import com.football.fieldservice.model.FieldStatus;
import com.football.fieldservice.model.FieldType;
import com.football.fieldservice.repository.FieldRepository;

import jakarta.annotation.PostConstruct;

/**
 * Nap 4 san mau dung theo bang du lieu chuan o docs/PHAN_CONG_KT1.md muc 1.
 *
 * CA NHOM DUNG CHUNG BO DU LIEU NAY. Khong ai tu che san rieng, neu khong
 * video cua 4 nguoi se mau thuan so lieu.
 *
 * Chi nap khi bang rong, nen khoi dong lai bao nhieu lan cung khong bi trung.
 * Viet bang Java thay vi data.sql de chay duoc tren ca H2 lan MySQL.
 *
 * Dung @PostConstruct chu KHONG dung CommandLineRunner: CommandLineRunner chay
 * SAU khi Tomcat da mo cong, nen co mot khoang vai tram mili giay ma GET
 * /api/fields tra ve mang rong. Da gap that khi chay thu. @PostConstruct chay
 * trong luc khoi tao bean, tuc la truoc khi cong 8082 nhan request dau tien.
 */
@Component
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final FieldRepository fieldRepository;

    public DataSeeder(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }

    @PostConstruct
    public void napDuLieuMau() {
        long daCo = fieldRepository.count();
        if (daCo > 0) {
            log.info("Bang fields da co {} ban ghi, bo qua buoc nap du lieu mau.", daCo);
            return;
        }

        fieldRepository.save(new Field("Sân số 1", FieldType.SAN_5, 200_000L, 300_000L, FieldStatus.ACTIVE));
        fieldRepository.save(new Field("Sân số 2", FieldType.SAN_7, 300_000L, 450_000L, FieldStatus.ACTIVE));
        // San so 3 co y de MAINTENANCE: dung lam ca kiem thu loi 409 trong demo
        fieldRepository.save(new Field("Sân số 3", FieldType.SAN_7, 300_000L, 450_000L, FieldStatus.MAINTENANCE));
        fieldRepository.save(new Field("Sân số 4", FieldType.SAN_11, 600_000L, 900_000L, FieldStatus.ACTIVE));

        log.info("Da nap {} san mau vao field_db.", fieldRepository.count());
    }
}
