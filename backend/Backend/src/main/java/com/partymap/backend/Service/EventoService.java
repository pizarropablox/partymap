package com.partymap.backend.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.partymap.backend.model.Evento;
import com.partymap.backend.model.Ubicacion;

/**
 * Interfaz de servicio para la gestión de eventos.
 * Proporciona métodos para operaciones CRUD de eventos y gestión de ubicaciones.
 */
public interface EventoService {

    /**
     * Obtiene todos los eventos del sistema
     * @return Lista de todos los eventos
     */
    List<Evento> getAllEvento();

    /**
     * Busca un evento por su ID
     * @param id ID del evento a buscar
     * @return Optional con el evento si existe
     */
    Optional<Evento> getEventoById(Long id);

    /**
     * Crea un nuevo evento con su ubicación
     * Si el evento incluye datos de ubicación, se crea automáticamente
     * @param evento Evento a crear
     * @return Evento creado con ubicación
     * @throws IOException Si hay error en la creación
     */
    Evento createEvento(Evento evento) throws IOException;

    /**
     * Crea un evento con ubicación específica
     * @param evento Evento a crear
     * @param ubicacion Ubicación del evento
     * @return Evento creado
     * @throws IOException Si hay error en la creación
     */
    Evento createEventoConUbicacion(Evento evento, Ubicacion ubicacion) throws IOException;

    /**
     * Actualiza un evento existente
     * @param id ID del evento a actualizar
     * @param evento Datos actualizados del evento
     * @return Evento actualizado
     */
    Evento updateEvento(Long id, Evento evento);

    /**
     * Actualiza un evento existente con ubicación
     * @param id ID del evento a actualizar
     * @param evento Datos actualizados del evento
     * @param ubicacion Datos actualizados de la ubicación
     * @return Evento actualizado
     */
    Evento updateEventoConUbicacion(Long id, Evento evento, Ubicacion ubicacion);

    /**
     * Elimina un evento del sistema
     * @param evento Evento a eliminar
     * @throws IOException Si hay error en la eliminación
     */
    void deleteEvento(Evento evento) throws IOException;

    /**
     * Obtiene eventos por ID de usuario
     * @param usuarioId ID del usuario
     * @return Lista de eventos del usuario
     */
    List<Evento> getEventosByUsuarioId(Long usuarioId);
}
