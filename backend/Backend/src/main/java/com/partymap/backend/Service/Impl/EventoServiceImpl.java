package com.partymap.backend.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partymap.backend.exceptions.NotFoundException;
import com.partymap.backend.model.Evento;
import com.partymap.backend.model.Ubicacion;
import com.partymap.backend.repository.EventoRepository;
import com.partymap.backend.repository.UbicacionRepository;
import com.partymap.backend.service.EventoService;

/**
 * Implementación del servicio de eventos.
 * Proporciona la lógica de negocio para la gestión de eventos.
 */
@Service
@Transactional
public class EventoServiceImpl implements EventoService {

    private final EventoRepository eventoRepository;
    private final UbicacionRepository ubicacionRepository;

    public EventoServiceImpl(EventoRepository eventoRepository, UbicacionRepository ubicacionRepository) {
        this.eventoRepository = eventoRepository;
        this.ubicacionRepository = ubicacionRepository;
    }

    /**
     * Obtiene todos los eventos activos
     */
    @Override
    public List<Evento> getAllEvento() {
        return eventoRepository.findAll().stream()
                .filter(evento -> evento.getActivo() == 1)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un evento por su ID
     */
    @Override
    public Optional<Evento> getEventoById(Long id) {
        return eventoRepository.findById(id);
    }

    /**
     * Crea un nuevo evento
     */
    @Override
    public Evento createEvento(Evento evento) throws IOException {
        return eventoRepository.save(evento);
    }

    /**
     * Crea un evento con ubicación (versión alternativa sin cascade)
     */
    @Override
    @Transactional
    public Evento createEventoConUbicacion(Evento evento, Ubicacion ubicacion) throws IOException {
        try {
            // Validar ubicación
            if (ubicacion == null) {
                throw new IllegalArgumentException("La ubicación no puede ser nula");
            }
            
            if (ubicacion.getDireccion() == null || ubicacion.getComuna() == null || 
                ubicacion.getLatitud() == null || ubicacion.getLongitud() == null) {
                throw new IllegalArgumentException("La ubicación debe tener dirección, comuna, latitud y longitud");
            }
            
            if (!ubicacion.coordenadasValidas()) {
                throw new IllegalArgumentException("Las coordenadas de la ubicación no son válidas");
            }
            
            // Guardar la ubicación
            Ubicacion ubicacionGuardada = ubicacionRepository.save(ubicacion);
            
            // Validar evento
            if (evento.getUsuario() == null) {
                throw new IllegalArgumentException("El evento debe tener un usuario asignado");
            }
            
            if (evento.getNombre() == null || evento.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del evento es obligatorio");
            }
            
            if (evento.getFecha() == null) {
                throw new IllegalArgumentException("La fecha del evento es obligatoria");
            }
            
            // Validar descripción (mínimo 10 caracteres, máximo 2000)
            if (evento.getDescripcion() == null || evento.getDescripcion().trim().isEmpty()) {
                throw new IllegalArgumentException("La descripción del evento es obligatoria");
            }
            
            if (evento.getDescripcion().trim().length() < 10) {
                throw new IllegalArgumentException("La descripción debe tener al menos 10 caracteres");
            }
            
            if (evento.getDescripcion().trim().length() > 2000) {
                throw new IllegalArgumentException("La descripción no puede exceder 2000 caracteres");
            }
            
            // Crear una nueva instancia del evento para evitar problemas de cascade
            Evento nuevoEvento = new Evento();
            nuevoEvento.setNombre(evento.getNombre());
            nuevoEvento.setDescripcion(evento.getDescripcion());
            nuevoEvento.setFecha(evento.getFecha());
            nuevoEvento.setCapacidadMaxima(evento.getCapacidadMaxima());
            nuevoEvento.setPrecioEntrada(evento.getPrecioEntrada());
            nuevoEvento.setImagenUrl(evento.getImagenUrl());
            nuevoEvento.setUbicacion(ubicacionGuardada);
            nuevoEvento.setUsuario(evento.getUsuario());
            nuevoEvento.setActivo(1);
            
            // Guardar el evento
            Evento eventoCreado = eventoRepository.save(nuevoEvento);
            
            return eventoCreado;
            
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Actualiza un evento existente
     */
    @Override
    public Evento updateEvento(Long id, Evento evento) {
        if (!eventoRepository.existsById(id)) {
            throw new NotFoundException("Evento no encontrado con ID: " + id);
        }
        
        // Si el evento tiene una nueva ubicación, la guardamos
        if (evento.getUbicacion() != null && evento.getUbicacion().getId() == null) {
            Ubicacion ubicacion = evento.getUbicacion();
            
            // Validar ubicación
            if (ubicacion.getDireccion() == null || ubicacion.getComuna() == null || 
                ubicacion.getLatitud() == null || ubicacion.getLongitud() == null) {
                throw new IllegalArgumentException("La ubicación debe tener dirección, comuna, latitud y longitud");
            }
            
            if (!ubicacion.coordenadasValidas()) {
                throw new IllegalArgumentException("Las coordenadas de la ubicación no son válidas");
            }
            
            // Guardar la nueva ubicación
            Ubicacion ubicacionGuardada = ubicacionRepository.save(ubicacion);
            evento.setUbicacion(ubicacionGuardada);
        }
        
        evento.setId(id);
        return eventoRepository.save(evento);
    }

    /**
     * Actualiza un evento existente con ubicación
     */
    @Override
    @Transactional
    public Evento updateEventoConUbicacion(Long id, Evento evento, Ubicacion ubicacion) {
        // Verificar que el evento existe
        Optional<Evento> eventoExistente = eventoRepository.findById(id);
        if (eventoExistente.isEmpty()) {
            throw new NotFoundException("Evento no encontrado con ID: " + id);
        }
        
        Evento eventoActual = eventoExistente.get();
        
        // Validar ubicación
        if (ubicacion == null) {
            throw new IllegalArgumentException("La ubicación no puede ser nula");
        }
        
        if (ubicacion.getDireccion() == null || ubicacion.getComuna() == null || 
            ubicacion.getLatitud() == null || ubicacion.getLongitud() == null) {
            throw new IllegalArgumentException("La ubicación debe tener dirección, comuna, latitud y longitud");
        }
        
        if (!ubicacion.coordenadasValidas()) {
            throw new IllegalArgumentException("Las coordenadas de la ubicación no son válidas");
        }
        
        // Validar evento
        if (evento.getNombre() == null || evento.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio");
        }
        
        if (evento.getFecha() == null) {
            throw new IllegalArgumentException("La fecha del evento es obligatoria");
        }
        
        // Validar descripción (mínimo 10 caracteres, máximo 2000)
        if (evento.getDescripcion() == null || evento.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del evento es obligatoria");
        }
        
        if (evento.getDescripcion().trim().length() < 10) {
            throw new IllegalArgumentException("La descripción debe tener al menos 10 caracteres");
        }
        
        if (evento.getDescripcion().trim().length() > 2000) {
            throw new IllegalArgumentException("La descripción no puede exceder 2000 caracteres");
        }
        
        // Actualizar la ubicación existente o crear una nueva
        Ubicacion ubicacionActualizada;
        if (eventoActual.getUbicacion() != null) {
            // Actualizar ubicación existente
            Ubicacion ubicacionExistente = eventoActual.getUbicacion();
            ubicacionExistente.setDireccion(ubicacion.getDireccion());
            ubicacionExistente.setComuna(ubicacion.getComuna());
            ubicacionExistente.setLatitud(ubicacion.getLatitud());
            ubicacionExistente.setLongitud(ubicacion.getLongitud());
            ubicacionActualizada = ubicacionRepository.save(ubicacionExistente);
        } else {
            // Crear nueva ubicación
            ubicacionActualizada = ubicacionRepository.save(ubicacion);
        }
        
        // Actualizar el evento
        eventoActual.setNombre(evento.getNombre());
        eventoActual.setDescripcion(evento.getDescripcion());
        eventoActual.setFecha(evento.getFecha());
        eventoActual.setCapacidadMaxima(evento.getCapacidadMaxima());
        eventoActual.setPrecioEntrada(evento.getPrecioEntrada());
        eventoActual.setImagenUrl(evento.getImagenUrl());
        eventoActual.setUbicacion(ubicacionActualizada);
        
        // Guardar el evento actualizado
        return eventoRepository.save(eventoActual);
    }

    /**
     * Elimina un evento del sistema (soft delete)
     */
    @Override
    public void deleteEvento(Evento evento) throws IOException {
        if (!eventoRepository.existsById(evento.getId())) {
            throw new NotFoundException("Evento no encontrado con ID: " + evento.getId());
        }
        
        // Soft delete: cambiar estado activo a 0
        evento.setActivo(0);
        eventoRepository.save(evento);
    }

    /**
     * Obtiene eventos por ID de usuario
     */
    @Override
    public List<Evento> getEventosByUsuarioId(Long usuarioId) {
        return eventoRepository.findByUsuarioId(usuarioId).stream()
                .filter(evento -> evento.getActivo() == 1)
                .collect(Collectors.toList());
    }
}
