package com.microservicio_bff_main.controller;    

import java.util.map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@CrossOrigin
@RequestMapping("/microservicio")
public class MicroservicioController {

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Integracion OK - POST");
        response.put("body", body);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> read(@PathVariable("id") String id) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Integracion OK - GET");
        response.put("id", id);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Map<String, String>> update(@RequestParam("status") String status) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Integracion OK - PUT");
        response.put("status", status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> delete(@RequestHeader("Authorization") String authHeader) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Integracion OK - DELETE");
        response.put("authHeader", authHeader);
        return ResponseEntity.ok(response);
    }
  

}

