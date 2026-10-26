package com.football.common.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cau hinh bao mat TAM THOI cho giai doan dung base.
 *
 * Vi 4 service deu keo theo spring-boot-starter-security, neu khong co cau hinh nay
 * thi Spring se tu dong chan het moi endpoint va bat dang nhap bang form - ca nhom
 * se khong goi duoc API nao bang Postman.
 *
 * Lop nay mo het moi request, chi bat khi:  app.security.mode = open  (mac dinh).
 *
 * KHI HOANG LAM XONG JWT:
 *   1. Viet lop SecurityConfig moi, danh dau @ConditionalOnProperty(name = "app.security.mode",
 *      havingValue = "jwt") va gan JwtAuthFilter vao chuoi loc.
 *   2. Doi  app.security.mode=jwt  trong application.properties cua tung service.
 *   3. Lop nay tu dong tat, KHONG can xoa.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "app.security.mode", havingValue = "open", matchIfMissing = true)
public class OpenSecurityConfig {

    @Bean
    public SecurityFilterChain openFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable())) // cho phep mo /h2-console
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
