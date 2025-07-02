package com.partymap.backend.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuestas de error estandarizadas.
 * Proporciona información detallada sobre errores para facilitar el debugging.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    
    /**
     * Timestamp del error
     */
    private LocalDateTime timestamp;
    
    /**
     * Código de estado HTTP
     */
    private int status;
    
    /**
     * Tipo de error
     */
    private String error;
    
    /**
     * Mensaje descriptivo del error
     */
    private String message;
    
    /**
     * Ruta donde ocurrió el error
     */
    private String path;
    
    /**
     * Constructor para errores de validación
     */
    public ErrorResponseDTO(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
    
    /**
     * Constructor para errores simples
     */
    public ErrorResponseDTO(String message) {
        this.timestamp = LocalDateTime.now();
        this.status = 400;
        this.error = "Bad Request";
        this.message = message;
        this.path = "";
    }
} 