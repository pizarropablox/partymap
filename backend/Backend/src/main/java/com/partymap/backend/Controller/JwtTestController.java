package com.partymap.backend.Controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.partymap.backend.Model.Usuario;
import com.partymap.backend.Service.UsuarioService;

/**
 * Controlador para pruebas de JWT y sincronización de usuarios.
 * Solo para desarrollo y testing.
 */
@RestController
@RequestMapping("/test")
public class JwtTestController {

    private static final Logger logger = LoggerFactory.getLogger(JwtTestController.class);

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Endpoint de prueba para verificar que JWT funciona
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "JWT authentication working!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para verificar autenticación y obtener información del JWT
     */
    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> auth(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            
            response.put("authenticated", true);
            response.put("subject", jwt.getSubject());
            response.put("email", jwt.getClaimAsString("emails"));
            response.put("given_name", jwt.getClaimAsString("given_name"));
            response.put("family_name", jwt.getClaimAsString("family_name"));
            response.put("extension_Roles", jwt.getClaimAsString("extension_Roles"));
            response.put("issuer", jwt.getIssuer());
            response.put("audience", jwt.getAudience());
            response.put("issued_at", jwt.getIssuedAt());
            response.put("expires_at", jwt.getExpiresAt());
            
            logger.info("JWT claims: {}", response);
        } else {
            response.put("authenticated", false);
            response.put("message", "No JWT token found");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para probar la sincronización de usuarios desde JWT
     */
    @GetMapping("/sync-user")
    public ResponseEntity<Map<String, Object>> syncUser(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                
                // Sincronizar usuario desde JWT
                Usuario usuario = usuarioService.sincronizarUsuarioDesdeJWT(jwt);
                
                response.put("success", true);
                response.put("message", "Usuario sincronizado correctamente");
                response.put("usuario", Map.of(
                    "id", usuario.getId(),
                    "email", usuario.getEmail(),
                    "nombre", usuario.getNombre(),
                    "tipoUsuario", usuario.getTipoUsuario(),
                    "azureB2cId", usuario.getAzureB2cId(),
                    "nombreAzure", usuario.getNombreAzure(),
                    "apellidoAzure", usuario.getApellidoAzure(),
                    "rolAzure", usuario.getRolAzure(),
                    "esUsuarioAzure", usuario.getEsUsuarioAzure()
                ));
                
                logger.info("Usuario sincronizado: {} ({})", usuario.getEmail(), usuario.getTipoUsuario());
            } else {
                response.put("success", false);
                response.put("message", "No JWT token found");
            }
        } catch (Exception e) {
            logger.error("Error sincronizando usuario", e);
            response.put("success", false);
            response.put("message", "Error sincronizando usuario: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para obtener información del usuario actual desde la base de datos
     */
    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                String email = ((java.util.List<String>) jwt.getClaim("emails")).get(0);
                
                var usuarioOpt = usuarioService.getUsuarioByEmail(email);
                
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    response.put("success", true);
                    response.put("usuario", Map.of(
                        "id", usuario.getId(),
                        "email", usuario.getEmail(),
                        "nombre", usuario.getNombre(),
                        "tipoUsuario", usuario.getTipoUsuario(),
                        "azureB2cId", usuario.getAzureB2cId(),
                        "nombreAzure", usuario.getNombreAzure(),
                        "apellidoAzure", usuario.getApellidoAzure(),
                        "rolAzure", usuario.getRolAzure(),
                        "esUsuarioAzure", usuario.getEsUsuarioAzure()
                    ));
                } else {
                    response.put("success", false);
                    response.put("message", "Usuario no encontrado en la base de datos");
                }
            } else {
                response.put("success", false);
                response.put("message", "No JWT token found");
            }
        } catch (Exception e) {
            logger.error("Error obteniendo usuario actual", e);
            response.put("success", false);
            response.put("message", "Error obteniendo usuario: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para probar la validación de roles
     */
    @GetMapping("/test-role-validation")
    public ResponseEntity<Map<String, Object>> testRoleValidation(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                
                // Obtener rol original del JWT
                String rolOriginal = jwt.getClaimAsString("extension_Roles");
                
                // Sincronizar usuario (esto aplicará la validación de roles)
                Usuario usuario = usuarioService.sincronizarUsuarioDesdeJWT(jwt);
                
                response.put("success", true);
                response.put("message", "Validación de roles completada");
                response.put("rolOriginal", rolOriginal);
                response.put("rolNormalizado", usuario.getRolAzure());
                response.put("tipoUsuario", usuario.getTipoUsuario());
                response.put("usuario", Map.of(
                    "id", usuario.getId(),
                    "email", usuario.getEmail(),
                    "nombre", usuario.getNombre(),
                    "tipoUsuario", usuario.getTipoUsuario(),
                    "rolAzure", usuario.getRolAzure()
                ));
                
                logger.info("Validación de roles: '{}' -> '{}' -> {}", 
                    rolOriginal, usuario.getRolAzure(), usuario.getTipoUsuario());
            } else {
                response.put("success", false);
                response.put("message", "No JWT token found");
            }
        } catch (Exception e) {
            logger.error("Error en validación de roles", e);
            response.put("success", false);
            response.put("message", "Error en validación de roles: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
} 