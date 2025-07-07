package com.partymap.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferencia de datos de Evento.
 * Contiene los campos necesarios para crear y actualizar eventos.
 * 
 * USO:
 * - Crear nuevo evento: enviar todos los campos excepto id
 * - Actualizar evento: enviar id + campos a modificar
 * - Incluye validaciones para nombre, descripción, fecha y relaciones
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoDTO {
    
    /**
     * ID del evento (opcional para creación, requerido para actualización)
     */
    private Long id;
    
    /**
     * Nombre o título del evento
     */
    @NotBlank(message = "El nombre del evento es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre del evento debe tener entre 3 y 100 caracteres")
    private String nombre;
    
    /**
     * Descripción detallada del evento
     */
    @NotBlank(message = "La descripción del evento es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String descripcion;
    
    /**
     * Fecha y hora programada para el evento
     */
    @NotNull(message = "La fecha del evento es obligatoria")
    @Future(message = "La fecha del evento debe ser futura")
    private LocalDateTime fecha;
    
    /**
     * Número máximo de personas que pueden asistir al evento
     */
    private Integer capacidadMaxima;
    
    /**
     * Precio de entrada al evento
     */
    private BigDecimal precioEntrada;
    
    /**
     * URL de la imagen promocional del evento
     */
    private String imagenUrl;
    
    /**
     * ID de la ubicación donde se realizará el evento
     */
    @NotNull(message = "La ubicación es obligatoria")
    private Long ubicacionId;
    
    /**
     * ID del usuario organizador del evento (productor o administrador)
     * Los eventos se crean para que los clientes los puedan ver y reservar
     */
    @NotNull(message = "El usuario organizador es obligatorio")
    private Long usuarioId;
    
    // Getters manuales para asegurar compatibilidad
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public LocalDateTime getFecha() { return fecha; }
    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public BigDecimal getPrecioEntrada() { return precioEntrada; }
    public String getImagenUrl() { return imagenUrl; }
    public Long getUbicacionId() { return ubicacionId; }
    public Long getUsuarioId() { return usuarioId; }
    
    // Setters manuales para asegurar compatibilidad
    public void setId(Long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void setCapacidadMaxima(Integer capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
    public void setPrecioEntrada(BigDecimal precioEntrada) { this.precioEntrada = precioEntrada; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public void setUbicacionId(Long ubicacionId) { this.ubicacionId = ubicacionId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
} 