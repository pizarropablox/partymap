package com.partymap.backend.dto;

import java.time.LocalDateTime;

import com.partymap.backend.model.TipoUsuario;

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
     * ID único de Azure B2C (sub claim del JWT)
     */
    private String azureB2cId;
    
    /**
     * Nombre del usuario desde Azure B2C (given_name)
     */
    private String nombreAzure;
    
    /**
     * Apellido del usuario desde Azure B2C (family_name)
     */
    private String apellidoAzure;
    
    /**
     * Rol desde Azure B2C (extension_Roles)
     */
    private String rolAzure;
    
    /**
     * RUT del productor desde Azure B2C (extension_RUT) - solo para usuarios tipo PRODUCTOR
     */
    private String rutProductor;
    
    /**
     * Indica si el usuario fue creado desde Azure B2C
     */
    private Boolean esUsuarioAzure;
    
    /**
     * Fecha de la última conexión del usuario
     */
    private LocalDateTime fechaUltimaConexion;
    
    /**
     * Indica si el usuario está activo en el sistema
     */
    private Integer activo;
    
    /**
     * Fecha de creación del usuario
     */
    private LocalDateTime fechaCreacion;
    
    // Getters manuales para asegurar compatibilidad
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public TipoUsuario getTipoUsuario() { return tipoUsuario; }
    public String getAzureB2cId() { return azureB2cId; }
    public String getNombreAzure() { return nombreAzure; }
    public String getApellidoAzure() { return apellidoAzure; }
    public String getRolAzure() { return rolAzure; }
    public String getRutProductor() { return rutProductor; }
    public Boolean getEsUsuarioAzure() { return esUsuarioAzure; }
    public LocalDateTime getFechaUltimaConexion() { return fechaUltimaConexion; }
    public Integer getActivo() { return activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    
    // Setters manuales para asegurar compatibilidad
    public void setId(Long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEmail(String email) { this.email = email; }
    public void setTipoUsuario(TipoUsuario tipoUsuario) { this.tipoUsuario = tipoUsuario; }
    public void setAzureB2cId(String azureB2cId) { this.azureB2cId = azureB2cId; }
    public void setNombreAzure(String nombreAzure) { this.nombreAzure = nombreAzure; }
    public void setApellidoAzure(String apellidoAzure) { this.apellidoAzure = apellidoAzure; }
    public void setRolAzure(String rolAzure) { this.rolAzure = rolAzure; }
    public void setRutProductor(String rutProductor) { this.rutProductor = rutProductor; }
    public void setEsUsuarioAzure(Boolean esUsuarioAzure) { this.esUsuarioAzure = esUsuarioAzure; }
    public void setFechaUltimaConexion(LocalDateTime fechaUltimaConexion) { this.fechaUltimaConexion = fechaUltimaConexion; }
    public void setActivo(Integer activo) { this.activo = activo; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
} 