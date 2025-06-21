package com.partymap.backend.Service;

import java.util.List;
import java.util.Optional;

import com.partymap.backend.Model.Usuario;

/**
 * Interfaz de servicio para la gestión de usuarios.
 * Proporciona métodos para operaciones CRUD de usuarios.
 */
public interface UsuarioService {

    /**
     * Obtiene todos los usuarios del sistema
     * @return Lista de todos los usuarios
     */
    List<Usuario> getAllUsuarios();

    /**
     * Busca un usuario por su ID
     * @param id ID del usuario a buscar
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> getUsuarioById(Long id);

    /**
     * Busca un usuario por su email
     * @param email Email del usuario a buscar
     * @return Optional con el usuario si existe
     */
    Optional<Usuario> getUsuarioByEmail(String email);

    /**
     * Verifica si existe un usuario con el email especificado
     * @param email Email a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);
} 