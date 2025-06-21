package com.partymap.backend.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Evento.
 * Contiene información completa del evento incluyendo ubicación y productor.
 * 
 * USO:
 * - Respuesta de consultas de evento (GET)
 * - Incluye metadatos de auditoría y estado activo
 * - Incluye información de ubicación y productor relacionados
 * - Incluye campos calculados como cupos disponibles y disponibilidad
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoResponseDTO {
    
    /**
     * Identificador único del evento
     */
    private Long id;
    
    /**
     * Nombre o título del evento
     */
    private String nombre;
    
    /**
     * Descripción detallada del evento
     */
    private String descripcion;
    
    /**
     * Fecha y hora programada para el evento
     */
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
     * Indica si el evento está activo
     */
    private Integer activo;
    
    /**
     * Fecha de creación del evento
     */
    private LocalDateTime fechaCreacion;
    
    /**
     * Fecha de última modificación del evento
     */
    private LocalDateTime fechaModificacion;
    
    /**
     * Información de la ubicación relacionada
     */
    private UbicacionResponseDTO ubicacion;
    
    /**
     * Información del productor relacionado
     */
    private ProductorResponseDTO productor;
    
    /**
     * Cupos disponibles para el evento
     */
    private Integer cuposDisponibles;
    
    /**
     * Indica si el evento está disponible para reservas
     */
    private Boolean disponible;
    
    /**
     * Indica si el evento ya pasó
     */
    private Boolean eventoPasado;
    
    /**
     * Indica si el evento está próximo (en los próximos 7 días)
     */
    private Boolean eventoProximo;
} 