package com.partymap.backend.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Ubicacion.
 * Contiene todos los campos de la ubicación para mostrar al cliente.
 * 
 * USO:
 * - Respuesta de consultas de ubicación (GET)
 * - Incluye metadatos de auditoría y estado activo
 * - Se usa en listas y detalles de ubicaciones
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionResponseDTO {
    
    /**
     * Identificador único de la ubicación
     */
    private Long id;
    
    /**
     * Dirección específica del lugar
     */
    private String direccion;
    
    /**
     * Comuna o distrito
     */
    private String comuna;
    
    /**
     * Coordenada de latitud
     */
    private BigDecimal latitud;
    
    /**
     * Coordenada de longitud
     */
    private BigDecimal longitud;
    
    /**
     * Indica si la ubicación está activa
     */
    private Boolean activo;
    
    /**
     * Fecha de creación de la ubicación
     */
    private LocalDateTime fechaCreacion;
    
    /**
     * Fecha de última modificación de la ubicación
     */
    private LocalDateTime fechaModificacion;
} 