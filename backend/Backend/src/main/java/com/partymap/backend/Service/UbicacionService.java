package com.partymap.backend.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.partymap.backend.Model.Ubicacion;

/**
 * Interfaz de servicio para la gestión de ubicaciones.
 * Proporciona métodos para operaciones CRUD de ubicaciones y funcionalidades adicionales.
 */
public interface UbicacionService {

    /**
     * Obtiene todas las ubicaciones del sistema
     * @return Lista de todas las ubicaciones
     */
    List<Ubicacion> getAllUbicaciones();

    /**
     * Busca una ubicación por su ID
     * @param id ID de la ubicación a buscar
     * @return Optional con la ubicación si existe
     */
    Optional<Ubicacion> getUbicacionById(Long id);

    /**
     * Crea una nueva ubicación con validaciones
     * @param ubicacion Ubicación a crear
     * @return Ubicación creada
     * @throws IOException Si hay error en la creación
     */
    Ubicacion createUbicacion(Ubicacion ubicacion) throws IOException;

    /**
     * Actualiza una ubicación existente
     * @param id ID de la ubicación a actualizar
     * @param ubicacion Datos actualizados de la ubicación
     * @return Ubicación actualizada
     */
    Ubicacion updateUbicacion(Long id, Ubicacion ubicacion);

    /**
     * Elimina una ubicación del sistema
     * @param ubicacion Ubicación a eliminar
     * @throws IOException Si hay error en la eliminación
     */
    void deleteUbicacion(Ubicacion ubicacion) throws IOException;

    /**
     * Obtiene todas las ubicaciones de una comuna específica
     * @param comuna Nombre de la comuna
     * @return Lista de ubicaciones en la comuna
     */
    List<Ubicacion> getUbicacionesByComuna(String comuna);

    /**
     * Valida si las coordenadas son válidas
     * @param latitud Coordenada de latitud
     * @param longitud Coordenada de longitud
     * @return true si las coordenadas son válidas, false en caso contrario
     */
    boolean validarCoordenadas(Double latitud, Double longitud);
} 