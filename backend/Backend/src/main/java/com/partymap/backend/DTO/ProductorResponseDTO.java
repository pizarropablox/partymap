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
    private Integer activo;
    
    /**
     * Fecha de creación del productor
     */
    private LocalDateTime fechaCreacion;
    
    /**
     * Información del usuario asociado
     */
    private UsuarioResponseDTO usuario;
    
    // Getters manuales para asegurar compatibilidad
    public Long getId() { return id; }
    public String getNombreEmpresa() { return nombreEmpresa; }
    public String getRut() { return rut; }
    public Integer getActivo() { return activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public UsuarioResponseDTO getUsuario() { return usuario; }
    
    // Setters manuales para asegurar compatibilidad
    public void setId(Long id) { this.id = id; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }
    public void setRut(String rut) { this.rut = rut; }
    public void setActivo(Integer activo) { this.activo = activo; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setUsuario(UsuarioResponseDTO usuario) { this.usuario = usuario; }
} 