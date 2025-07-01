package com.partymap.backend.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de prueba para verificar que CORS funciona correctamente
 * desde localhost:4200
 */
@RestController
@RequestMapping("/cors-test")
public class CorsTestController {

    /**
     * Endpoint de prueba para verificar CORS
     * GET /cors-test/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CORS test successful!");
        response.put("timestamp", System.currentTimeMillis());
        response.put("origin", "http://localhost:4200");
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de prueba con headers personalizados
     * GET /cors-test/headers
     */
    @GetMapping("/headers")
    public ResponseEntity<Map<String, Object>> testHeaders() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Headers test successful!");
        response.put("cors-enabled", true);
        response.put("allowed-origins", "http://localhost:4200");
        response.put("allowed-methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH");
        response.put("allowed-headers", "*,Authorization,Content-Type,Accept,Origin,X-Requested-With");
        response.put("allow-credentials", true);
        return ResponseEntity.ok(response);
    }
} 