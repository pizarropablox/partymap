package com.partymap.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear un evento con ubicación en una sola petición.
 * Contiene tanto los datos del evento como los de la ubicación.
 * 
 * USO:
 * - Crear evento con ubicación nueva en una sola operación
 * - Enviar como JSON en POST /evento/con-ubicacion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoConUbicacionDTO {
    
    /**
     * Datos del evento a crear
     */
    private EventoDTO evento;
    
    /**
     * Datos de la ubicación donde se realizará el evento
     */
    private UbicacionDTO ubicacion;
} 