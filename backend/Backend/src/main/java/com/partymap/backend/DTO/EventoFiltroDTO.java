package com.partymap.backend.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para filtrar eventos con diferentes criterios de búsqueda.
 * Permite buscar eventos por fecha, ubicación, precio, etc.
 * 
 * USO:
 * - Enviar como parámetro en búsquedas avanzadas de eventos
 * - Permite paginación y filtros combinados
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoFiltroDTO {
    
    /**
     * Nombre del evento (búsqueda parcial)
     */
    private String nombre;
    
    /**
     * Comuna donde se realiza el evento
     */
    private String comuna;
    
    /**
     * Fecha mínima del evento
     */
    private LocalDateTime fechaDesde;
    
    /**
     * Fecha máxima del evento
     */
    private LocalDateTime fechaHasta;
    
    /**
     * Precio mínimo de entrada
     */
    private BigDecimal precioMinimo;
    
    /**
     * Precio máximo de entrada
     */
    private BigDecimal precioMaximo;
    
    /**
     * ID del productor
     */
    private Long productorId;
    
    /**
     * Solo eventos disponibles (con cupos y no pasados)
     */
    private Boolean soloDisponibles;
    
    /**
     * Número de página para paginación
     */
    private Integer pagina;
    
    /**
     * Tamaño de página para paginación
     */
    private Integer tamanio;
} 