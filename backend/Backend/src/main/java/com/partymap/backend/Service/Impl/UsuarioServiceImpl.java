package com.partymap.backend.Service.Impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partymap.backend.Model.Usuario;
import com.partymap.backend.Repository.UsuarioRepository;
import com.partymap.backend.Service.UsuarioService;

/**
 * Implementación del servicio de usuarios.
 * Gestiona las operaciones CRUD de usuarios.
 * Incluye funcionalidades para sincronizar usuarios desde Azure B2C.
 */
@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtiene todos los usuarios activos del sistema
     */
    @Override
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getActivo() == 1)
                .collect(Collectors.toList());
    }

    /**
     * Busca un usuario por su ID
     */
    @Override
    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Busca un usuario por su email
     */
    @Override
    public Optional<Usuario> getUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Busca un usuario por su ID de Azure B2C
     */
    @Override
    public Optional<Usuario> getUsuarioByAzureB2cId(String azureB2cId) {
        return usuarioRepository.findByAzureB2cId(azureB2cId);
    }

    /**
     * Verifica si existe un usuario con el email especificado
     */
    @Override
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    /**
     * Crea un nuevo usuario
     */
    @Override
    public Usuario createUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    /**
     * Actualiza un usuario existente
     */
    @Override
    public Usuario updateUsuario(Long id, Usuario usuario) {
        if (usuarioRepository.existsById(id)) {
            usuario.setId(id);
            return usuarioRepository.save(usuario);
        }
        throw new RuntimeException("Usuario no encontrado con ID: " + id);
    }

    /**
     * Elimina un usuario existente
     */
    @Override
    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    /**
     * Sincroniza un usuario desde un JWT de Azure B2C
     */
    @Override
    public Usuario sincronizarUsuarioDesdeJWT(Jwt jwt) {
        // Extraer información del JWT
        String email = ((List<String>) jwt.getClaim("emails")).get(0);
        String nombreAzure = jwt.getClaimAsString("given_name");
        String apellidoAzure = jwt.getClaimAsString("family_name");
        String azureB2cId = jwt.getSubject();
        String rolAzure = jwt.getClaimAsString("extension_Roles");

        // Buscar usuario por Azure B2C ID primero
        Optional<Usuario> usuarioExistente = getUsuarioByAzureB2cId(azureB2cId);
        
        if (usuarioExistente.isPresent()) {
            // Usuario existe, actualizar información
            Usuario usuario = usuarioExistente.get();
            usuario.actualizarDesdeAzureB2C(nombreAzure, apellidoAzure, rolAzure);
            return usuarioRepository.save(usuario);
        } else {
            // Buscar por email como respaldo
            usuarioExistente = getUsuarioByEmail(email);
            
            if (usuarioExistente.isPresent()) {
                // Usuario existe por email pero no tiene Azure B2C ID, actualizar
                Usuario usuario = usuarioExistente.get();
                usuario.setAzureB2cId(azureB2cId);
                usuario.actualizarDesdeAzureB2C(nombreAzure, apellidoAzure, rolAzure);
                return usuarioRepository.save(usuario);
            } else {
                // Crear nuevo usuario
                return findOrCreateUsuarioByEmail(email, nombreAzure, apellidoAzure, azureB2cId, rolAzure);
            }
        }
    }

    /**
     * Encuentra o crea un usuario por email
     */
    @Override
    public Usuario findOrCreateUsuarioByEmail(String email, String nombreAzure, String apellidoAzure, String azureB2cId, String rolAzure) {
        Optional<Usuario> usuarioExistente = getUsuarioByEmail(email);
        
        if (usuarioExistente.isPresent()) {
            // Usuario existe, actualizar información de Azure B2C
            Usuario usuario = usuarioExistente.get();
            usuario.setAzureB2cId(azureB2cId);
            usuario.actualizarDesdeAzureB2C(nombreAzure, apellidoAzure, rolAzure);
            return usuarioRepository.save(usuario);
        } else {
            // Crear nuevo usuario
            Usuario nuevoUsuario = new Usuario(email, nombreAzure, apellidoAzure, azureB2cId, rolAzure);
            return usuarioRepository.save(nuevoUsuario);
        }
    }
} 