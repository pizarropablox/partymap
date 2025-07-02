package com.partymap.backend.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el proceso de autenticación (login).
 * Contiene las credenciales necesarias para autenticar un usuario.
 * 
 * USO:
 * - Enviar email y contraseña para iniciar sesión
 * - Validaciones automáticas de formato de email y contraseña
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    
    /**
     * Email del usuario
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;
    
    /**
     * Contraseña del usuario
     */
    @NotBlank(message = "La contraseña es obligatoria")
    private String contraseña;
} 