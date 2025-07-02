package com.partymap.backend.DTO;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferencia de datos de Reserva.
 * Contiene los campos necesarios para crear y actualizar reservas.
 * 
 * USO:
 * - Crear nueva reserva: enviar todos los campos excepto id
 * - Actualizar reserva: enviar id + campos a modificar
 * - Incluye validaciones para cantidad, usuario y evento
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {
    
    /**
     * ID de la reserva (opcional para creación, requerido para actualización)
     */
    private Long id;
    
    /**
     * Número de entradas reservadas
     */
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
    
    /**
     * Precio por entrada individual (opcional, puede calcularse en backend)
     */
    private BigDecimal precioUnitario;
    
    /**
     * Comentarios adicionales de la reserva
     */
    private String comentarios;
    
    /**
     * ID del usuario que realiza la reserva
     */
    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;
    
    /**
     * ID del evento para el cual se realiza la reserva
     */
    @NotNull(message = "El evento es obligatorio")
    private Long eventoId;
    
    // Getters y setters manuales para asegurar compatibilidad
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    
    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }
    
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    
    public Long getEventoId() { return eventoId; }
    public void setEventoId(Long eventoId) { this.eventoId = eventoId; }
} 