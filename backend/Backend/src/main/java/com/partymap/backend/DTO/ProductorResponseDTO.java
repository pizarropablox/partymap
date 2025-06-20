package com.partymap.backend.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Productor.
 * Contiene información completa del productor incluyendo datos del usuario asociado.
 * 
 * USO:
 * - Respuesta de consultas de productor (GET)
 * - Incluye metadatos de auditoría y estado activo
 * - Incluye información del usuario asociado
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductorResponseDTO {
    
    /**
     * Identificador único del productor
     */
    private Long id;
    
    /**
     * Nombre de la empresa o razón social
     */
    private String nombreEmpresa;
    
    /**
     * RUT único de la empresa
     */
    private String rut;
    
    /**
     * Indica si el productor está activo
     */
    private Boolean activo;
    
    /**
     * Fecha de creación del productor
     */
    private LocalDateTime fechaCreacion;
    
    /**
     * Fecha de última modificación del productor
     */
    private LocalDateTime fechaModificacion;
    
    /**
     * Información del usuario asociado
     */
    private UsuarioResponseDTO usuario;
} 