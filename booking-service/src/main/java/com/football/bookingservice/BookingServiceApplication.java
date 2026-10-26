package com.football.bookingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * booking-service - cong 8081 - chu so huu: LINH
 *
 * Dich vu trung tam: nhan yeu cau dat san cua khach, hoi field-service xem san
 * co dat duoc khong va het bao nhieu tien, roi luu luot dat vao booking_db.
 *
 * Can lam tiep (xem docs/PHAN_CONG_KT1.md muc 3 va docs/api-contract.md):
 *   model/Booking.java            : @Entity - id, customerUsername, fieldId, fieldName,
 *                                   bookingDate, startTime, endTime, totalPrice, status, createdAt
 *   model/BookingStatus.java      : PENDING | CONFIRMED | PAID | CANCELLED
 *   dto/BookingRequest.java       : CHI nhan fieldId, bookingDate, startTime, endTime
 *                                   (customerUsername lay tu JWT, totalPrice hoi field-service)
 *   dto/BookingResponse.java      : du lieu tra ve cho khach
 *   dto/StatisticsResponse.java   : thong ke theo trang thai + doanh thu
 *   repository/BookingRepository.java
 *   client/FieldClient.java       : goi field-service (phan cua Hung)
 *   service/BookingService.java   : quy tac R1-R9, chong trung lich
 *   controller/BookingController.java
 *   exception/GlobalExceptionHandler.java : tra loi dung dinh dang o api-contract.md muc 0,
 *                                           phu du 400 / 404 / 409 / 503 / 504
 *
 * Danh sach endpoint phai co (api-contract.md):
 *   POST   /api/bookings              dat san              201 / 400 / 404 / 409 / 503
 *   GET    /api/bookings/me           luot dat cua toi     200 / 401
 *   GET    /api/bookings              toan bo (chu san)    200 / 403
 *   GET    /api/bookings/{id}         chi tiet             200 / 403 / 404
 *   PUT    /api/bookings/{id}/cancel  huy san              200 / 403 / 409
 *   PUT    /api/bookings/{id}/status  payment-service goi  200 / 400 / 404
 *   GET    /api/bookings/statistics   doanh thu (chu san)  200 / 403
 *
 * scanBasePackages phai co "com.football.common" thi module common-security
 * (JWT, cau hinh bao mat, /health) moi duoc nap vao.
 */
@SpringBootApplication(scanBasePackages = { "com.football.bookingservice", "com.football.common" })
public class BookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }
}
