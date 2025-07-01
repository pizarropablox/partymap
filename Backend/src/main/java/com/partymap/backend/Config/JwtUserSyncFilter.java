package com.partymap.backend.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.partymap.backend.Model.Usuario;
import com.partymap.backend.Service.UsuarioService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtro que sincroniza automáticamente usuarios desde JWT de Azure B2C.
 * Se ejecuta después de la autenticación JWT para asegurar que el usuario
 * existe en la base de datos local.
 */
@Component
public class JwtUserSyncFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtUserSyncFilter.class);

    @Autowired
    private UsuarioService usuarioService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // Verificar si hay un usuario autenticado
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                
                // Sincronizar usuario desde JWT
                Usuario usuario = usuarioService.sincronizarUsuarioDesdeJWT(jwt);
                
                logger.debug("Usuario sincronizado: {} ({})", usuario.getEmail(), usuario.getTipoUsuario());
            }
        } catch (Exception e) {
            logger.error("Error sincronizando usuario desde JWT", e);
            // No interrumpir el flujo por errores de sincronización
        }
        
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // No aplicar el filtro a endpoints de salud o públicos
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || 
               path.startsWith("/error") || 
               path.equals("/favicon.ico");
    }
} 