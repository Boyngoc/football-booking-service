package com.football.fieldservice.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.football.fieldservice.dto.QuoteResponse;
import com.football.fieldservice.exception.InvalidRequestException;
import com.football.fieldservice.model.Field;

/**
 * Tinh tien thue san - PHAN LOI CUA field-service.
 *
 * Day la ly do booking-service khong duoc tu tinh tien: bang gia va khung gio
 * vang la du lieu cua field-service. Doi gia hay doi khung gio vang chi sua o
 * day, khong dich vu nao khac phai sua theo.
 *
 * Cach tinh: cat khoang thue thanh hai phan theo TUNG PHUT
 *   - phut roi vao [goldenStart, goldenEnd)  -> tinh theo goldenPricePerHour
 *   - phut con lai                            -> tinh theo pricePerHour
 * Khong lam tron ca ca, nen da vat qua 17:00 thi chi phan sau 17:00 bi tinh gia vang.
 *
 * Vi du doi chieu (xem docs/api-contract.md muc 1.2):
 *   San 2 (300k / 450k)  17:00-18:30  ->   0h thuong + 1.5h vang =   675.000
 *   San 2 (300k / 450k)  16:00-18:00  ->   1h thuong +   1h vang =   750.000
 *   San 1 (200k / 300k)  08:00-09:30  -> 1.5h thuong +   0h vang =   300.000
 *   San 4 (600k / 900k)  19:00-21:00  ->   0h thuong +   2h vang = 1.800.000
 */
@Service
public class PricingService {

    private final LocalTime openTime;
    private final LocalTime closeTime;
    private final LocalTime goldenStart;
    private final LocalTime goldenEnd;

    public PricingService(
            @Value("${app.field.open-time}") String openTime,
            @Value("${app.field.close-time}") String closeTime,
            @Value("${app.field.golden-start}") String goldenStart,
            @Value("${app.field.golden-end}") String goldenEnd) {
        this.openTime = LocalTime.parse(openTime);
        this.closeTime = LocalTime.parse(closeTime);
        this.goldenStart = LocalTime.parse(goldenStart);
        this.goldenEnd = LocalTime.parse(goldenEnd);
    }

    public QuoteResponse quote(Field field, LocalDate date, LocalTime start, LocalTime end) {
        kiemTraKhungGio(start, end);

        long tongPhut = Duration.between(start, end).toMinutes();
        long phutVang = soPhutGiaoNhau(start, end, goldenStart, goldenEnd);
        long phutThuong = tongPhut - phutVang;

        // Nhan truoc roi moi chia 60 de tranh sai so khi le nua tieng
        double tien = (phutThuong * field.getPricePerHour() + phutVang * field.getGoldenPricePerHour()) / 60.0;

        return new QuoteResponse(
                field.getId(),
                field.getName(),
                date,
                start,
                end,
                lamTron2(phutThuong / 60.0),
                lamTron2(phutVang / 60.0),
                Math.round(tien));
    }

    /**
     * field-service so huu gio mo cua, nen chinh no tu choi khung gio khong hop le.
     * booking-service chi viec chuyen tiep ma 400 nay cho khach (quy tac R3, R4).
     */
    private void kiemTraKhungGio(LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw new InvalidRequestException("Giờ kết thúc phải sau giờ bắt đầu");
        }
        if (start.isBefore(openTime) || end.isAfter(closeTime)) {
            throw new InvalidRequestException(
                    "Cụm sân chỉ mở cửa từ " + openTime + " đến " + closeTime);
        }
    }

    /** So phut giao nhau giua hai khoang nua mo [a1, a2) va [b1, b2). */
    private long soPhutGiaoNhau(LocalTime a1, LocalTime a2, LocalTime b1, LocalTime b2) {
        LocalTime batDau = a1.isAfter(b1) ? a1 : b1;
        LocalTime ketThuc = a2.isBefore(b2) ? a2 : b2;
        if (!ketThuc.isAfter(batDau)) {
            return 0;
        }
        return Duration.between(batDau, ketThuc).toMinutes();
    }

    private double lamTron2(double giaTri) {
        return Math.round(giaTri * 100.0) / 100.0;
    }
}
