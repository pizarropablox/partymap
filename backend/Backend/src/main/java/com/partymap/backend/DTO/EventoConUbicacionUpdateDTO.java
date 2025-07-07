package com.partymap.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar un evento con ubicación en una sola petición.
 * Contiene tanto los datos del evento como los de la ubicación.
 * 
 * USO:
 * - Actualizar evento con ubicación en una sola operación
 * - Enviar como JSON en PUT /evento/{id}/con-ubicacion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoConUbicacionUpdateDTO {
    
    /**
     * Datos del evento a actualizar
     */
    private EventoDTO evento;
    
    /**
     * Datos de la ubicación donde se realizará el evento
     */
    private UbicacionDTO ubicacion;
    
    // Getters manuales para asegurar compatibilidad
    public EventoDTO getEvento() { return evento; }
    public UbicacionDTO getUbicacion() { return ubicacion; }
    
    // Setters manuales para asegurar compatibilidad
    public void setEvento(EventoDTO evento) { this.evento = evento; }
    public void setUbicacion(UbicacionDTO ubicacion) { this.ubicacion = ubicacion; }
} 