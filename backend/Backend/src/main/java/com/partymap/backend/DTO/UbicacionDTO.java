package com.partymap.backend.DTO;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferencia de datos de Ubicacion.
 * Contiene los campos necesarios para crear y actualizar ubicaciones.
 * 
 * USO:
 * - Crear nueva ubicación: enviar todos los campos excepto id
 * - Actualizar ubicación: enviar id + campos a modificar
 * - Incluye validaciones para dirección, comuna y coordenadas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionDTO {
    
    /**
     * ID de la ubicación (opcional para creación, requerido para actualización)
     */
    private Long id;
    
    /**
     * Dirección específica del lugar
     */
    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 150, message = "La dirección debe tener entre 5 y 150 caracteres")
    private String direccion;
    
    /**
     * Comuna o distrito
     */
    @NotBlank(message = "La comuna es obligatoria")
    @Size(min = 2, max = 50, message = "La comuna debe tener entre 2 y 50 caracteres")
    private String comuna;
    
    /**
     * Coordenada de latitud
     */
    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private BigDecimal latitud;
    
    /**
     * Coordenada de longitud
     */
    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private BigDecimal longitud;
    
    // Getters manuales para asegurar compatibilidad
    public Long getId() { return id; }
    public String getDireccion() { return direccion; }
    public String getComuna() { return comuna; }
    public BigDecimal getLatitud() { return latitud; }
    public BigDecimal getLongitud() { return longitud; }
    
    // Setters manuales para asegurar compatibilidad
    public void setId(Long id) { this.id = id; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setComuna(String comuna) { this.comuna = comuna; }
    public void setLatitud(BigDecimal latitud) { this.latitud = latitud; }
    public void setLongitud(BigDecimal longitud) { this.longitud = longitud; }
} 