package com.partymap.backend.Service.Impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partymap.backend.Exceptions.NotFoundException;
import com.partymap.backend.Model.Ubicacion;
import com.partymap.backend.Repository.UbicacionRepository;
import com.partymap.backend.Service.UbicacionService;

/**
 * Implementación del servicio de ubicaciones.
 * Gestiona las operaciones CRUD de ubicaciones y validaciones de coordenadas.
 */
@Service
@Transactional
public class UbicacionServiceImpl implements UbicacionService {

    private final UbicacionRepository ubicacionRepository;

    public UbicacionServiceImpl(UbicacionRepository ubicacionRepository) {
        this.ubicacionRepository = ubicacionRepository;
    }

    /**
     * Obtiene todas las ubicaciones activas del sistema
     */
    @Override
    public List<Ubicacion> getAllUbicaciones() {
        return ubicacionRepository.findAll().stream()
                .filter(ubicacion -> ubicacion.getActivo() == 1)
                .collect(Collectors.toList());
    }

    /**
     * Busca una ubicación por su ID
     */
    @Override
    public Optional<Ubicacion> getUbicacionById(Long id) {
        return ubicacionRepository.findById(id);
    }

    /**
     * Crea una nueva ubicación con validaciones
     */
    @Override
    public Ubicacion createUbicacion(Ubicacion ubicacion) throws IOException {
        // Validar que la ubicación tenga los datos requeridos
        if (ubicacion.getDireccion() == null || ubicacion.getComuna() == null || 
            ubicacion.getLatitud() == null || ubicacion.getLongitud() == null) {
            throw new IllegalArgumentException("La ubicación debe tener dirección, comuna, latitud y longitud");
        }
        
        // Validar coordenadas
        if (!ubicacion.coordenadasValidas()) {
            throw new IllegalArgumentException("Las coordenadas de la ubicación no son válidas");
        }
        
        // Verificar si ya existe una ubicación con la misma dirección y comuna
        if (ubicacionRepository.existsByDireccionAndComunaIgnoreCase(ubicacion.getDireccion(), ubicacion.getComuna())) {
            throw new IllegalArgumentException("Ya existe una ubicación con la misma dirección y comuna");
        }
        
        // Guardar la ubicación
        return ubicacionRepository.save(ubicacion);
    }

    /**
     * Actualiza una ubicación existente
     */
    @Override
    public Ubicacion updateUbicacion(Long id, Ubicacion ubicacion) {
        if (!ubicacionRepository.existsById(id)) {
            throw new NotFoundException("Ubicación no encontrada con ID: " + id);
        }
        
        // Validar coordenadas si se están actualizando
        if (ubicacion.getLatitud() != null && ubicacion.getLongitud() != null) {
            if (!validarCoordenadas(ubicacion.getLatitud().doubleValue(), ubicacion.getLongitud().doubleValue())) {
                throw new IllegalArgumentException("Las coordenadas de la ubicación no son válidas");
            }
        }
        
        ubicacion.setId(id);
        return ubicacionRepository.save(ubicacion);
    }

    /**
     * Elimina una ubicación del sistema (soft delete)
     */
    @Override
    public void deleteUbicacion(Ubicacion ubicacion) throws IOException {
        if (!ubicacionRepository.existsById(ubicacion.getId())) {
            throw new NotFoundException("Ubicación no encontrada con ID: " + ubicacion.getId());
        }
        
        // Verificar si la ubicación tiene eventos asociados
        if (!ubicacion.getEventos().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar una ubicación que tiene eventos asociados");
        }
        
        // Soft delete: cambiar estado activo a 0
        ubicacion.setActivo(0);
        ubicacionRepository.save(ubicacion);
    }

    /**
     * Obtiene todas las ubicaciones de una comuna específica
     */
    @Override
    public List<Ubicacion> getUbicacionesByComuna(String comuna) {
        return ubicacionRepository.findByComunaContainingIgnoreCase(comuna);
    }

    /**
     * Valida si las coordenadas son válidas
     */
    @Override
    public boolean validarCoordenadas(Double latitud, Double longitud) {
        if (latitud == null || longitud == null) {
            return false;
        }
        
        // Validar rangos de coordenadas
        return latitud >= -90.0 && latitud <= 90.0 &&
               longitud >= -180.0 && longitud <= 180.0;
    }
} 