package com.football.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * auth-service - cong 8080 - chu so huu: HOANG
 *
 * Can lam tiep (xem docs/PHAN_CONG_KT1.md muc 3):
 *   model/User.java, model/Role.java        : CUSTOMER | OWNER
 *   repository/UserRepository.java
 *   service/AuthService.java                : dang ky, dang nhap, ma hoa BCrypt
 *   controller/AuthController.java          : POST /api/auth/register, /api/auth/login, GET /api/auth/me
 *   chuyen tiep sang common-security        : JwtService, JwtAuthFilter, SecurityConfig
 */
@SpringBootApplication(scanBasePackages = { "com.football.authservice", "com.football.common" })
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
