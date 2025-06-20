package com.partymap.backend.Service.Impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partymap.backend.Exceptions.NotFoundException;
import com.partymap.backend.Model.Evento;
import com.partymap.backend.Model.Ubicacion;
import com.partymap.backend.Repository.EventoRepository;
import com.partymap.backend.Repository.UbicacionRepository;
import com.partymap.backend.Service.EventoService;

/**
 * Implementación del servicio de eventos.
 * Gestiona las operaciones CRUD de eventos y la creación automática de ubicaciones.
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
     * Obtiene todos los eventos del sistema
     */
    @Override
    public List<Evento> getAllEvento() {
        return eventoRepository.findAll();
    }

    /**
     * Busca un evento por su ID
     */
    @Override
    public Optional<Evento> getEventoById(Long id) {
        return eventoRepository.findById(id);
    }

    /**
     * Crea un nuevo evento con ubicación automática
     * Si el evento tiene una ubicación asociada, se crea automáticamente
     */
    @Override
    public Evento createEvento(Evento evento) throws IOException {
        // Si el evento tiene una ubicación, la guardamos primero
        if (evento.getUbicacion() != null) {
            Ubicacion ubicacion = evento.getUbicacion();
            
            // Validar que la ubicación tenga los datos requeridos
            if (ubicacion.getDireccion() == null || ubicacion.getComuna() == null || 
                ubicacion.getLatitud() == null || ubicacion.getLongitud() == null) {
                throw new IllegalArgumentException("La ubicación debe tener dirección, comuna, latitud y longitud");
            }
            
            // Validar coordenadas
            if (!ubicacion.coordenadasValidas()) {
                throw new IllegalArgumentException("Las coordenadas de la ubicación no son válidas");
            }
            
            // Guardar la ubicación
            Ubicacion ubicacionGuardada = ubicacionRepository.save(ubicacion);
            evento.setUbicacion(ubicacionGuardada);
        }
        
        // Guardar el evento
        return eventoRepository.save(evento);
    }

    /**
     * Crea un evento con una ubicación específica
     */
    @Override
    public Evento createEventoConUbicacion(Evento evento, Ubicacion ubicacion) throws IOException {
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
        
        // Asignar la ubicación al evento
        evento.setUbicacion(ubicacionGuardada);
        
        // Guardar el evento
        return eventoRepository.save(evento);
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
     * Elimina un evento del sistema
     */
    @Override
    public void deleteEvento(Evento evento) throws IOException {
        if (!eventoRepository.existsById(evento.getId())) {
            throw new NotFoundException("Evento no encontrado con ID: " + evento.getId());
        }
        eventoRepository.deleteById(evento.getId());
    }
}
