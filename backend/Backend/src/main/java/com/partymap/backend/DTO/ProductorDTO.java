package com.partymap.backend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferencia de datos de Productor.
 * Contiene los campos necesarios para crear y actualizar productores.
 * 
 * USO:
 * - Crear nuevo productor: enviar todos los campos excepto id
 * - Actualizar productor: enviar id + campos a modificar
 * - Incluye validaciones para nombre de empresa y RUT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductorDTO {
    
    /**
     * ID del productor (opcional para creación, requerido para actualización)
     */
    private Long id;
    
    /**
     * Nombre de la empresa o razón social
     */
    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre de la empresa debe tener entre 2 y 100 caracteres")
    private String nombreEmpresa;
    
    /**
     * RUT único de la empresa (formato chileno)
     */
    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^[0-9]{7,8}-[0-9kK]$", message = "El formato del RUT no es válido")
    private String rut;
    
    /**
     * ID del usuario asociado a este productor
     */
    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;
    
    // Getters manuales para asegurar compatibilidad
    public Long getId() { return id; }
    public String getNombreEmpresa() { return nombreEmpresa; }
    public String getRut() { return rut; }
    public Long getUsuarioId() { return usuarioId; }
    
    // Setters manuales para asegurar compatibilidad
    public void setId(Long id) { this.id = id; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }
    public void setRut(String rut) { this.rut = rut; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
} 