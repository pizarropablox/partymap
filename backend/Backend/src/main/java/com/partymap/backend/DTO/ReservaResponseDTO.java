package com.partymap.backend.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.partymap.backend.Model.EstadoReserva;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Reserva.
 * Contiene información completa de la reserva incluyendo usuario y evento.
 * 
 * USO:
 * - Respuesta de consultas de reserva (GET)
 * - Incluye metadatos de auditoría y estado activo
 * - Incluye información de usuario y evento relacionados
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponseDTO {
    
    /**
     * Identificador único de la reserva
     */
    private Long id;
    
    /**
     * Número de entradas reservadas
     */
    private Integer cantidad;
    
    /**
     * Fecha y hora en que se realizó la reserva
     */
    private LocalDateTime fechaReserva;
    
    /**
     * Precio por entrada individual
     */
    private BigDecimal precioUnitario;
    
    /**
     * Precio total de la reserva
     */
    private BigDecimal precioTotal;
    
    /**
     * Comentarios adicionales de la reserva
     */
    private String comentarios;
    
    /**
     * Estado actual de la reserva
     */
    private EstadoReserva estado;
    
    /**
     * Indica si la reserva está activa
     */
    private Integer activo;
    
    /**
     * Fecha de creación de la reserva
     */
    private LocalDateTime fechaCreacion;
    
    /**
     * Fecha de última modificación de la reserva
     */
    private LocalDateTime fechaModificacion;
    
    /**
     * Información del usuario que realizó la reserva
     */
    private UsuarioResponseDTO usuario;
    
    /**
     * Información del evento para el cual se realizó la reserva
     */
    private EventoResponseDTO evento;
} 