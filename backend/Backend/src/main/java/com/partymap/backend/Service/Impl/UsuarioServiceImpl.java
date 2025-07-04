package com.partymap.backend.Service.Impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 🔧 Constructor público agregado para testing (sin romper @Autowired)
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

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
     * Busca un usuario productor por su RUT
     */
    @Override
    public Optional<Usuario> getProductorByRut(String rutProductor) {
        return usuarioRepository.findByRutProductor(rutProductor);
    }

    /**
     * Verifica si existe un usuario con el email especificado
     */
    @Override
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    /**
     * Verifica si existe un usuario productor con el RUT especificado
     */
    @Override
    public boolean existsProductorByRut(String rutProductor) {
        return usuarioRepository.existsByRutProductor(rutProductor);
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
     * Obtiene todos los usuarios productores activos
     */
    @Override
    public List<Usuario> getAllProductores() {
        return usuarioRepository.findProductoresActivos();
    }

    /**
     * Busca usuarios productores por nombre
     */
    @Override
    public List<Usuario> getProductoresByNombre(String nombre) {
        return usuarioRepository.findProductoresByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Sincroniza un usuario desde un JWT de Azure B2C
     */
    @Override
    public Usuario sincronizarUsuarioDesdeJWT(Jwt jwt) {
        // Extraer información del JWT
        Object emailsClaim = jwt.getClaim("emails");
        String email;
        if (emailsClaim instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> emails = (List<String>) emailsClaim;
            email = emails.get(0);
        } else if (emailsClaim instanceof String) {
            email = (String) emailsClaim;
        } else {
            throw new RuntimeException("Formato de email no válido en JWT");
        }
        
        String nombreAzure = jwt.getClaimAsString("given_name");
        String apellidoAzure = jwt.getClaimAsString("family_name");
        String azureB2cId = jwt.getSubject();
        String rolAzure = jwt.getClaimAsString("extension_Roles");
        
        // Extraer RUT del productor de forma segura
        Object rutClaim = jwt.getClaim("extension_RUT");
        String rutProductor = null;
        if (rutClaim != null) {
            if (rutClaim instanceof String) {
                rutProductor = (String) rutClaim;
            } else {
                rutProductor = rutClaim.toString();
            }
        }

        // Validar y normalizar el rol
        String rolNormalizado = validarYNormalizarRol(rolAzure);

        // Buscar usuario por Azure B2C ID primero
        Optional<Usuario> usuarioExistente = getUsuarioByAzureB2cId(azureB2cId);
        
        if (usuarioExistente.isPresent()) {
            // Usuario existe, actualizar información
            Usuario usuario = usuarioExistente.get();
            usuario.actualizarDesdeAzureB2C(nombreAzure, apellidoAzure, rolNormalizado, rutProductor);
            return usuarioRepository.save(usuario);
        } else {
            // Buscar por email como respaldo
            usuarioExistente = getUsuarioByEmail(email);
            
            if (usuarioExistente.isPresent()) {
                // Usuario existe por email pero no tiene Azure B2C ID, actualizar
                Usuario usuario = usuarioExistente.get();
                usuario.setAzureB2cId(azureB2cId);
                usuario.actualizarDesdeAzureB2C(nombreAzure, apellidoAzure, rolNormalizado, rutProductor);
                return usuarioRepository.save(usuario);
            } else {
                // Crear nuevo usuario
                return findOrCreateUsuarioByEmail(email, nombreAzure, apellidoAzure, azureB2cId, rolNormalizado, rutProductor);
            }
        }
    }

    /**
     * Valida y normaliza el rol de usuario
     * - Convierte a mayúsculas el valor del campo extension_Roles
     * - Valida que sea CLIENTE, ADMINISTRADOR o PRODUCTOR
     * - Si no es válido, asigna CLIENTE por defecto
     * 
     * @param rolAzure Rol original del JWT (extension_Roles)
     * @return Rol normalizado y validado
     */
    private String validarYNormalizarRol(String rolAzure) {
        // Convertir a mayúsculas y eliminar espacios
        String rolNormalizado = rolAzure.trim().toUpperCase();
        
        // Validar que sea uno de los roles permitidos
        switch (rolNormalizado) {
            case "CLIENTE":
                return rolNormalizado;
            case "ADMINISTRADOR":
                return rolNormalizado;
            case "PRODUCTOR":
                return rolNormalizado;
            default:
                // Si el rol no es válido, asignar CLIENTE por defecto
                return "CLIENTE";
        }
    }

    /**
     * Encuentra o crea un usuario por email
     */
    @Override
    public Usuario findOrCreateUsuarioByEmail(String email, String nombreAzure, String apellidoAzure, String azureB2cId, String rolAzure, String rutProductor) {
        // Validar y normalizar el rol
        String rolNormalizado = validarYNormalizarRol(rolAzure);
        
        Optional<Usuario> usuarioExistente = getUsuarioByEmail(email);
        
        if (usuarioExistente.isPresent()) {
            // Usuario existe, actualizar información de Azure B2C
            Usuario usuario = usuarioExistente.get();
            usuario.setAzureB2cId(azureB2cId);
            usuario.actualizarDesdeAzureB2C(nombreAzure, apellidoAzure, rolNormalizado, rutProductor);
            return usuarioRepository.save(usuario);
        } else {
            // Crear nuevo usuario
            Usuario nuevoUsuario = new Usuario(email, nombreAzure, apellidoAzure, azureB2cId, rolNormalizado, rutProductor);
            return usuarioRepository.save(nuevoUsuario);
        }
    }
} 