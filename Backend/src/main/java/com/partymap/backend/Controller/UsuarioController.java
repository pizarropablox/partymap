package com.partymap.backend.Controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.partymap.backend.Config.SecurityUtils;
import com.partymap.backend.DTO.UsuarioResponseDTO;
import com.partymap.backend.Exceptions.NotFoundException;
import com.partymap.backend.Model.Usuario;
import com.partymap.backend.Service.UsuarioService;

/**
 * Controlador REST para la gestión de usuarios.
 * Proporciona endpoints para consultar información de usuarios.
 * 
 * SEGURIDAD:
 * - ADMINISTRADOR: Acceso completo a todos los usuarios
 * - PRODUCTOR: Puede ver información básica de usuarios
 * - CLIENTE: Solo puede ver su propia información
 */
@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SecurityUtils securityUtils;

    public UsuarioController(UsuarioService usuarioService, SecurityUtils securityUtils) {
        this.usuarioService = usuarioService;
        this.securityUtils = securityUtils;
    }

    /**
     * Obtiene todos los usuarios del sistema
     * GET /usuario/all
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR puede ver todos los usuarios
     */
    @GetMapping("/all")
    public ResponseEntity<List<UsuarioResponseDTO>> getAllUsuarios() {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores pueden ver todos los usuarios
        if (!user.isAdministrador()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        List<UsuarioResponseDTO> usuariosDTO = usuarios.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(usuariosDTO);
    }

    /**
     * Obtiene un usuario específico por su ID
     * GET /usuario/{id}
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede ver cualquier usuario
     * - PRODUCTOR: Puede ver cualquier usuario
     * - CLIENTE: Solo puede ver su propia información
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Clientes solo pueden ver su propia información
        if (user.isCliente() && !user.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Usuario> usuario = usuarioService.getUsuarioById(id);
        if (usuario.isEmpty()) {
            throw new NotFoundException("Usuario no encontrado con ID: " + id);
        }

        return ResponseEntity.ok(convertToResponseDTO(usuario.get()));
    }

    /**
     * Obtiene un usuario por su email
     * GET /usuario/email/{email}
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede buscar cualquier usuario
     * - PRODUCTOR: Solo puede buscar su propia información
     * - CLIENTE: Solo puede buscar su propia información
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioByEmail(@PathVariable String email) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Productores y clientes solo pueden buscar su propia información
        if ((user.isProductor() || user.isCliente()) && !user.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Usuario> usuario = usuarioService.getUsuarioByEmail(email);
        if (usuario.isEmpty()) {
            throw new NotFoundException("Usuario no encontrado con email: " + email);
        }

        return ResponseEntity.ok(convertToResponseDTO(usuario.get()));
    }

    /**
     * Obtiene un usuario productor por su RUT
     * GET /usuario/productor/rut/{rut}
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede buscar cualquier productor
     * - PRODUCTOR: Solo puede buscar su propio RUT
     * - CLIENTE: No puede usar este endpoint
     */
    @GetMapping("/productor/rut/{rut}")
    public ResponseEntity<UsuarioResponseDTO> getProductorByRut(@PathVariable String rut) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Clientes no pueden usar este endpoint
        if (user.isCliente()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Productores solo pueden buscar su propio RUT
        if (user.isProductor() && !rut.equals(user.getRutProductor())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Usuario> productor = usuarioService.getProductorByRut(rut);
        if (productor.isEmpty()) {
            throw new NotFoundException("Productor no encontrado con RUT: " + rut);
        }

        return ResponseEntity.ok(convertToResponseDTO(productor.get()));
    }

    /**
     * Verifica si existe un productor con el RUT especificado
     * GET /usuario/productor/existe-rut
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR puede verificar RUTs
     */
    @GetMapping("/productor/existe-rut")
    public ResponseEntity<Boolean> existeProductorPorRut(@RequestParam String rut) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores pueden verificar RUTs
        if (!user.isAdministrador()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean existe = usuarioService.existsProductorByRut(rut);
        return ResponseEntity.ok(existe);
    }

    /**
     * Obtiene todos los usuarios productores
     * GET /usuario/productores
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR puede ver todos los productores
     */
    @GetMapping("/productores")
    public ResponseEntity<List<UsuarioResponseDTO>> getAllProductores() {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores pueden ver todos los productores
        if (!user.isAdministrador()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Usuario> productores = usuarioService.getAllProductores();
        List<UsuarioResponseDTO> productoresDTO = productores.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productoresDTO);
    }

    /**
     * Busca usuarios productores por nombre
     * GET /usuario/productores/buscar
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR puede buscar productores
     */
    @GetMapping("/productores/buscar")
    public ResponseEntity<List<UsuarioResponseDTO>> buscarProductores(@RequestParam String nombre) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores pueden buscar productores
        if (!user.isAdministrador()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Usuario> productores = usuarioService.getProductoresByNombre(nombre);
        List<UsuarioResponseDTO> productoresDTO = productores.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productoresDTO);
    }

    /**
     * Obtiene el usuario actual autenticado
     * GET /usuario/current
     * 
     * SEGURIDAD:
     * - Cualquier usuario autenticado puede ver su propia información
     */
    @GetMapping("/current")
    public ResponseEntity<UsuarioResponseDTO> getCurrentUsuario() {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(convertToResponseDTO(currentUser.get()));
    }

    /**
     * Busca usuarios por tipo
     * GET /usuario/tipo/{tipoUsuario}
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR puede usar este endpoint
     */
    @GetMapping("/tipo/{tipoUsuario}")
    public ResponseEntity<List<UsuarioResponseDTO>> getUsuariosByTipo(@PathVariable String tipoUsuario) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores pueden usar este endpoint
        if (!user.isAdministrador()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        List<UsuarioResponseDTO> usuariosDTO = usuarios.stream()
                .filter(u -> u.getTipoUsuario().toString().equalsIgnoreCase(tipoUsuario))
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(usuariosDTO);
    }

    /**
     * Busca usuarios activos
     * GET /usuario/activos
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR puede usar este endpoint
     */
    @GetMapping("/activos")
    public ResponseEntity<List<UsuarioResponseDTO>> getUsuariosActivos() {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores pueden usar este endpoint
        if (!user.isAdministrador()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        List<UsuarioResponseDTO> usuariosDTO = usuarios.stream()
                .filter(u -> u.getActivo() == 1)
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(usuariosDTO);
    }

    /**
     * Busca usuarios inactivos
     * GET /usuario/inactivos
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR puede usar este endpoint
     */
    @GetMapping("/inactivos")
    public ResponseEntity<List<UsuarioResponseDTO>> getUsuariosInactivos() {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores pueden usar este endpoint
        if (!user.isAdministrador()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        List<UsuarioResponseDTO> usuariosDTO = usuarios.stream()
                .filter(u -> u.getActivo() == 0)
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(usuariosDTO);
    }

    /**
     * Busca usuarios con filtros
     * GET /usuario/buscar
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR puede usar este endpoint
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioResponseDTO>> buscarUsuarios(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String email) {
        
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores pueden usar este endpoint
        if (!user.isAdministrador()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        
        // Aplicar filtros
        if (tipo != null) {
            usuarios = usuarios.stream()
                    .filter(u -> u.getTipoUsuario().toString().equalsIgnoreCase(tipo))
                    .collect(Collectors.toList());
        }
        
        if (activo != null) {
            usuarios = usuarios.stream()
                    .filter(u -> (activo && u.getActivo() == 1) || (!activo && u.getActivo() == 0))
                    .collect(Collectors.toList());
        }
        
        if (email != null) {
            usuarios = usuarios.stream()
                    .filter(u -> u.getEmail().toLowerCase().contains(email.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        List<UsuarioResponseDTO> usuariosDTO = usuarios.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(usuariosDTO);
    }

    /**
     * Obtiene estadísticas de usuarios
     * GET /usuario/estadisticas
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR puede usar este endpoint
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Object> getEstadisticasUsuarios() {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores pueden usar este endpoint
        if (!user.isAdministrador()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalUsuarios", usuarios.size());
        estadisticas.put("usuariosActivos", usuarios.stream().filter(u -> u.getActivo() == 1).count());
        estadisticas.put("usuariosInactivos", usuarios.stream().filter(u -> u.getActivo() == 0).count());
        estadisticas.put("productores", usuarios.stream().filter(u -> u.isProductor()).count());
        estadisticas.put("clientes", usuarios.stream().filter(u -> u.isCliente()).count());
        estadisticas.put("administradores", usuarios.stream().filter(u -> u.isAdministrador()).count());
        
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Sincroniza el rol de un usuario desde Azure B2C
     * POST /usuario/{id}/sync-rol
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR puede usar este endpoint
     */
    @PostMapping("/{id}/sync-rol")
    public ResponseEntity<Map<String, Object>> syncRolUsuario(@PathVariable Long id) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores pueden usar este endpoint
        if (!user.isAdministrador()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Usuario> usuario = usuarioService.getUsuarioById(id);
        if (usuario.isEmpty()) {
            throw new NotFoundException("Usuario no encontrado con ID: " + id);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Función de sincronización de rol no implementada en este endpoint");
        response.put("usuario", convertToResponseDTO(usuario.get()));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Convierte un Usuario a UsuarioResponseDTO
     */
    private UsuarioResponseDTO convertToResponseDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setTipoUsuario(usuario.getTipoUsuario());
        dto.setAzureB2cId(usuario.getAzureB2cId());
        dto.setNombreAzure(usuario.getNombreAzure());
        dto.setApellidoAzure(usuario.getApellidoAzure());
        dto.setRolAzure(usuario.getRolAzure());
        dto.setRutProductor(usuario.getRutProductor());
        dto.setEsUsuarioAzure(usuario.getEsUsuarioAzure());
        dto.setFechaUltimaConexion(usuario.getFechaUltimaConexion());
        dto.setActivo(usuario.getActivo());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        return dto;
    }
} 