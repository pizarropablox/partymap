package com.partymap.backend.Config;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.partymap.backend.Model.Usuario;
import com.partymap.backend.Service.UsuarioService;

/**
 * Clase utilitaria para manejar la autenticación y autorización.
 * Proporciona métodos para obtener el usuario actual del contexto de seguridad.
 */
@Component
public class SecurityUtils {

    private final UsuarioService usuarioService;

    public SecurityUtils(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Obtiene el usuario actual autenticado del contexto de seguridad.
     * @return Optional con el usuario si está autenticado, empty si no
     */
    public Optional<Usuario> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            
            // Intentar obtener email de diferentes claims
            String email = null;
            
            // Primero intentar con 'emails' (lista)
            try {
                Object emailsClaim = jwt.getClaim("emails");
                if (emailsClaim instanceof java.util.List) {
                    java.util.List<String> emails = (java.util.List<String>) emailsClaim;
                    if (!emails.isEmpty()) {
                        email = emails.get(0);
                    }
                }
            } catch (Exception e) {
                // Ignorar error y continuar con otros claims
            }
            
            // Si no se encontró, intentar con 'preferred_username'
            if (email == null) {
                email = jwt.getClaimAsString("preferred_username");
            }
            
            // Si no se encontró, intentar con 'email'
            if (email == null) {
                email = jwt.getClaimAsString("email");
            }
            
            if (email != null) {
                return usuarioService.getUsuarioByEmail(email);
            }
        }
        
        return Optional.empty();
    }

    /**
     * Obtiene el email del usuario actual autenticado.
     * @return Optional con el email si está autenticado, empty si no
     */
    public Optional<String> getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            
            // Intentar obtener email de diferentes claims
            String email = null;
            
            // Primero intentar con 'emails' (lista)
            try {
                Object emailsClaim = jwt.getClaim("emails");
                if (emailsClaim instanceof java.util.List) {
                    java.util.List<String> emails = (java.util.List<String>) emailsClaim;
                    if (!emails.isEmpty()) {
                        email = emails.get(0);
                    }
                }
            } catch (Exception e) {
                // Ignorar error y continuar con otros claims
            }
            
            // Si no se encontró, intentar con 'preferred_username'
            if (email == null) {
                email = jwt.getClaimAsString("preferred_username");
            }
            
            // Si no se encontró, intentar con 'email'
            if (email == null) {
                email = jwt.getClaimAsString("email");
            }
            
            return Optional.ofNullable(email);
        }
        
        return Optional.empty();
    }

    /**
     * Verifica si el usuario actual es un cliente.
     * @return true si es cliente, false en caso contrario
     */
    public boolean isCurrentUserCliente() {
        return getCurrentUser()
                .map(Usuario::isCliente)
                .orElse(false);
    }

    /**
     * Verifica si el usuario actual es un productor.
     * @return true si es productor, false en caso contrario
     */
    public boolean isCurrentUserProductor() {
        return getCurrentUser()
                .map(Usuario::isProductor)
                .orElse(false);
    }

    /**
     * Verifica si el usuario actual es un administrador.
     * @return true si es administrador, false en caso contrario
     */
    public boolean isCurrentUserAdministrador() {
        return getCurrentUser()
                .map(Usuario::isAdministrador)
                .orElse(false);
    }

    /**
     * Verifica si el usuario actual puede acceder a una reserva específica.
     * Un usuario puede acceder a su propia reserva, o si es administrador/productor.
     * @param reservaUsuarioId ID del usuario propietario de la reserva
     * @return true si puede acceder, false en caso contrario
     */
    public boolean canAccessReserva(Long reservaUsuarioId) {
        Optional<Usuario> currentUser = getCurrentUser();
        
        if (currentUser.isEmpty()) {
            return false;
        }

        Usuario user = currentUser.get();
        
        // Administradores y productores pueden acceder a todas las reservas
        if (user.isAdministrador() || user.isProductor()) {
            return true;
        }
        
        // Clientes solo pueden acceder a sus propias reservas
        return user.getId().equals(reservaUsuarioId);
    }

    /**
     * Verifica si el usuario actual puede modificar una reserva específica.
     * Un usuario puede modificar su propia reserva, o si es administrador.
     * @param reservaUsuarioId ID del usuario propietario de la reserva
     * @return true si puede modificar, false en caso contrario
     */
    public boolean canModifyReserva(Long reservaUsuarioId) {
        Optional<Usuario> currentUser = getCurrentUser();
        
        if (currentUser.isEmpty()) {
            return false;
        }

        Usuario user = currentUser.get();
        
        // Solo administradores pueden modificar cualquier reserva
        if (user.isAdministrador()) {
            return true;
        }
        
        // Clientes solo pueden modificar sus propias reservas
        return user.isCliente() && user.getId().equals(reservaUsuarioId);
    }
} 