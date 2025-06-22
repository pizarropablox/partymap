package cl.duoc.ejemplo.microservicio.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;


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


