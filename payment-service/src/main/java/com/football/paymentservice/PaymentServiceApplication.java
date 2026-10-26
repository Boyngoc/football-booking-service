package com.football.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * payment-service - cong 8083 - chu so huu: NHAT
 *
 * Can lam tiep (xem docs/PHAN_CONG_KT1.md muc 3):
 *   model/Payment.java, model/PaymentMethod.java  : MOMO | VNPAY | CASH
 *   model/PaymentStatus.java                      : INIT | SUCCESS | FAILED
 *   repository/PaymentRepository.java
 *   client/BookingClient.java                     : GOI API booking-service de lay so tien that
 *   controller/PaymentController.java             : POST /api/payments,
 *                                                   POST /api/payments/{id}/confirm,
 *                                                   GET /api/payments/booking/{bookingId}
 *
 * LUU Y QUAN TRONG: payment-service TUYET DOI khong ket noi vao booking_db.
 */
@SpringBootApplication(scanBasePackages = { "com.football.paymentservice", "com.football.common" })
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
