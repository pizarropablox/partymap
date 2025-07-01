package com.partymap.backend.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.oauth2.jwt.Jwt;

import com.partymap.backend.Model.Usuario;

/**
 * Servicio para la gestión de usuarios en el sistema PartyMap.
 * Incluye funcionalidades para sincronizar usuarios desde Azure B2C.
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
     * Obtiene un usuario por su Azure B2C ID
     */
    Optional<Usuario> getUsuarioByAzureB2cId(String azureB2cId);
    
    /**
     * Busca un usuario productor por su RUT
     * @param rutProductor RUT del productor a buscar
     * @return Optional con el usuario productor si existe
     */
    Optional<Usuario> getProductorByRut(String rutProductor);
    
    /**
     * Verifica si existe un usuario con el email especificado
     * @param email Email a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);
    
    /**
     * Verifica si existe un usuario productor con el RUT especificado
     * @param rutProductor RUT del productor a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsProductorByRut(String rutProductor);
    
    /**
     * Crea un nuevo usuario
     */
    Usuario createUsuario(Usuario usuario);
    
    /**
     * Actualiza un usuario existente
     */
    Usuario updateUsuario(Long id, Usuario usuario);
    
    /**
     * Elimina un usuario
     */
    void deleteUsuario(Long id);
    
    /**
     * Obtiene todos los usuarios productores activos
     * @return Lista de usuarios productores activos
     */
    List<Usuario> getAllProductores();
    
    /**
     * Busca usuarios productores por nombre
     * @param nombre Nombre a buscar
     * @return Lista de usuarios productores que coinciden
     */
    List<Usuario> getProductoresByNombre(String nombre);
    
    /**
     * Sincroniza o crea un usuario desde JWT de Azure B2C
     * Si el usuario no existe, lo crea. Si existe, lo actualiza.
     */
    Usuario sincronizarUsuarioDesdeJWT(Jwt jwt);
    
    /**
     * Busca un usuario por email y lo crea si no existe
     */
    Usuario findOrCreateUsuarioByEmail(String email, String nombreAzure, String apellidoAzure, String azureB2cId, String rolAzure, String rutProductor);
} 