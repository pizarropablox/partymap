package com.partymap.backend.DTO;

import com.partymap.backend.Model.TipoUsuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferencia de datos de Usuario.
 * Contiene los campos necesarios para crear y actualizar usuarios.
 * 
 * USO:
 * - Crear nuevo usuario: enviar todos los campos excepto id
 * - Actualizar usuario: enviar id + campos a modificar
 * - Validaciones automáticas de email, contraseña y tipo de usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    
    /**
     * ID del usuario (opcional para creación, requerido para actualización)
     */
    private Long id;
    
    /**
     * Nombre completo del usuario (2-100 caracteres)
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
    
    /**
     * Email único del usuario (formato válido, máximo 100 caracteres)
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;
    
    /**
     * Contraseña del usuario (6-100 caracteres, se encriptará)
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String contraseña;
    
    /**
     * Tipo de usuario que determina permisos en el sistema
     */
    @NotNull(message = "El tipo de usuario es obligatorio")
    private TipoUsuario tipoUsuario;
} 