package com.football.common.security;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint kiem tra dich vu con song, dung chung cho ca 4 service.
 *
 *   GET http://localhost:8080/health   -> auth-service
 *   GET http://localhost:8081/health   -> booking-service
 *   GET http://localhost:8082/health   -> field-service
 *   GET http://localhost:8083/health   -> payment-service
 *
 * Dung trong demo de chung minh ca 4 dich vu dang chay, va de Hung chung minh
 * field-service da bi tat khi dien tinh huong loi 503.
 */
@RestController
public class HealthController {

    @Value("${spring.application.name:unknown-service}")
    private String serviceName;

    @Value("${server.port:0}")
    private String port;

    @Value("${app.owner:chua-gan}")
    private String owner;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", serviceName);
        body.put("port", port);
        body.put("owner", owner);
        body.put("status", "UP");
        body.put("time", LocalDateTime.now().toString());
        return body;
    }
}
