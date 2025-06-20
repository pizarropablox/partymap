package com.partymap.backend.DTO;

import java.time.LocalDateTime;

import com.partymap.backend.Model.TipoUsuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Usuario.
 * Contiene los campos que se envían al cliente, excluyendo información sensible.
 * 
 * USO:
 * - Respuesta de consultas de usuario (GET)
 * - No incluye contraseña por seguridad
 * - Incluye metadatos de auditoría (fechas, estado activo)
 * - Se usa en listas y detalles de usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    
    /**
     * Identificador único del usuario
     */
    private Long id;
    
    /**
     * Nombre completo del usuario
     */
    private String nombre;
    
    /**
     * Email del usuario (sin contraseña por seguridad)
     */
    private String email;
    
    /**
     * Tipo de usuario (CLIENTE, PRODUCTOR, ADMINISTRADOR)
     */
    private TipoUsuario tipoUsuario;
    
    /**
     * Indica si el usuario está activo en el sistema
     */
    private Boolean activo;
    
    /**
     * Fecha de creación del usuario
     */
    private LocalDateTime fechaCreacion;
    
    /**
     * Fecha de última modificación del usuario
     */
    private LocalDateTime fechaModificacion;
} 