package com.partymap.backend.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.partymap.backend.Model.Productor;

/**
 * Interfaz de servicio para la gestión de productores.
 * Proporciona métodos para operaciones CRUD de productores y funcionalidades adicionales.
 */
public interface ProductorService {

    /**
     * Obtiene todos los productores del sistema
     * @return Lista de todos los productores
     */
    List<Productor> getAllProductor();

    /**
     * Busca un productor por su ID
     * @param id ID del productor a buscar
     * @return Optional con el productor si existe
     */
    Optional<Productor> getProductorById(Long id);

    /**
     * Busca un productor por su RUT
     * @param rut RUT del productor a buscar
     * @return Optional con el productor si existe
     */
    Optional<Productor> getProductorByRut(String rut);

    /**
     * Crea un nuevo productor con validaciones
     * @param productor Productor a crear
     * @return Productor creado
     * @throws IOException Si hay error en la creación
     */
    Productor createProductor(Productor productor) throws IOException;

    /**
     * Actualiza un productor existente
     * @param id ID del productor a actualizar
     * @param productor Datos actualizados del productor
     * @return Productor actualizado
     */
    Productor updateProductor(Long id, Productor productor);

    /**
     * Elimina un productor del sistema
     * @param productor Productor a eliminar
     * @throws IOException Si hay error en la eliminación
     */
    void deleteProductor(Productor productor) throws IOException;

    /**
     * Obtiene productores por nombre de empresa
     * @param nombreEmpresa Nombre de la empresa a buscar
     * @return Lista de productores que coinciden
     */
    List<Productor> getProductoresByNombreEmpresa(String nombreEmpresa);

    /**
     * Verifica si existe un productor con el RUT especificado
     * @param rut RUT a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByRut(String rut);

    /**
     * Valida que el RUT tenga un formato válido
     * @param rut RUT a validar
     * @return true si el formato es válido, false en caso contrario
     */
    boolean validarRut(String rut);
}
