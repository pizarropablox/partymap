package com.partymap.backend.DTO;

import java.time.LocalDateTime;

import com.partymap.backend.Model.EstadoReserva;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para filtrar reservas con diferentes criterios de búsqueda.
 * Permite buscar reservas por usuario, evento, estado, fecha, etc.
 * 
 * USO:
 * - Enviar como parámetro en búsquedas avanzadas de reservas
 * - Permite paginación y filtros combinados
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaFiltroDTO {
    
    /**
     * ID del usuario que realizó la reserva
     */
    private Long usuarioId;
    
    /**
     * ID del evento reservado
     */
    private Long eventoId;
    
    /**
     * Estado de la reserva (RESERVADA, CANCELADA)
     */
    private EstadoReserva estado;
    
    /**
     * Fecha mínima de la reserva
     */
    private LocalDateTime fechaDesde;
    
    /**
     * Fecha máxima de la reserva
     */
    private LocalDateTime fechaHasta;
    
    /**
     * Solo reservas activas
     */
    private Boolean soloActivas;
    
    /**
     * Número de página para paginación
     */
    private Integer pagina;
    
    /**
     * Tamaño de página para paginación
     */
    private Integer tamanio;
} 