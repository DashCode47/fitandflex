package com.backoffice.fitandflex.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
            "message", "Welcome to Fit & Flex API",
            "version", "1.0.0",
            "status", "running",
            "documentation", "/swagger-ui.html",
            "health", "/actuator/health"
        );
    }

    @GetMapping("/api")
    public Map<String, Object> apiInfo() {
        return Map.of(
            "message", "Fit & Flex API",
            "version", "1.0.0",
            "endpoints", Map.of(
                "auth", "/api/auth",
                "branches", "/api/branches",
                "docs", "/swagger-ui.html",
                "health", "/actuator/health"
            )
        );
    }
}
