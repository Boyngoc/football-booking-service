package com.football.bookingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * booking-service - cong 8081 - chu so huu: LINH
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
