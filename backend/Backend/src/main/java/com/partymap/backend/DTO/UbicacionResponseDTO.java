package com.partymap.backend.dto;

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
    private Integer activo;
    
    /**
     * Fecha de creación de la ubicación
     */
    private LocalDateTime fechaCreacion;
    
    // Getters manuales para asegurar compatibilidad
    public Long getId() { return id; }
    public String getDireccion() { return direccion; }
    public String getComuna() { return comuna; }
    public BigDecimal getLatitud() { return latitud; }
    public BigDecimal getLongitud() { return longitud; }
    public Integer getActivo() { return activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    
    // Setters manuales para asegurar compatibilidad
    public void setId(Long id) { this.id = id; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setComuna(String comuna) { this.comuna = comuna; }
    public void setLatitud(BigDecimal latitud) { this.latitud = latitud; }
    public void setLongitud(BigDecimal longitud) { this.longitud = longitud; }
    public void setActivo(Integer activo) { this.activo = activo; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
} 