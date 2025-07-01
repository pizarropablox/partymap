package com.partymap.backend.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa a un usuario en el sistema PartyMap.
 * Un usuario puede ser cliente, productor o administrador, cada uno con diferentes
 * permisos y funcionalidades. Los usuarios pueden hacer reservas y, si son productores,
 * pueden crear y gestionar eventos.
 * 
 * Sincronización con Azure B2C:
 * - Los usuarios se crean/actualizan automáticamente al recibir un token JWT
 * - El email se usa como identificador único
 * - Los roles se mapean desde extension_Roles del JWT
 * - El RUT del productor se obtiene desde extension_RUT del JWT
 */
@Entity
@Table(name = "USUARIO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Usuario extends BaseEntity {
    
    /**
     * Identificador único del usuario
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq")
    @SequenceGenerator(name = "usuario_seq", sequenceName = "USUARIO_SEQ", allocationSize = 1)
    private Long id;
    
    /**
     * Nombre completo del usuario
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;
    
    /**
     * Email único del usuario, usado para autenticación
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;
    
    /**
     * Tipo de usuario que determina sus permisos en el sistema
     */
    @NotNull(message = "El tipo de usuario es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", length = 20, nullable = false)
    private TipoUsuario tipoUsuario;
    
    /**
     * ID único de Azure B2C (sub claim del JWT)
     */
    @Column(name = "azure_b2c_id", length = 100, unique = true)
    private String azureB2cId;
    
    /**
     * Nombre del usuario desde Azure B2C (given_name)
     */
    @Column(name = "nombre_azure", length = 100)
    private String nombreAzure;
    
    /**
     * Apellido del usuario desde Azure B2C (family_name)
     */
    @Column(name = "apellido_azure", length = 100)
    private String apellidoAzure;
    
    /**
     * Rol desde Azure B2C (extension_Roles)
     */
    @Column(name = "rol_azure", length = 50)
    private String rolAzure;
    
    /**
     * RUT del productor desde Azure B2C (extension_RUT) - solo para usuarios tipo PRODUCTOR
     */
    @Column(name = "rut_productor", length = 20)
    private String rutProductor;
    
    /**
     * Indica si el usuario fue creado desde Azure B2C
     */
    @Column(name = "es_usuario_azure", nullable = false)
    private Boolean esUsuarioAzure = false;
    
    /**
     * Fecha de la última conexión del usuario
     */
    @Column(name = "fecha_ultima_conexion")
    private LocalDateTime fechaUltimaConexion;
    
    /**
     * Lista de eventos creados por el usuario (solo si el usuario es productor)
     */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Evento> eventos = new ArrayList<>();
    
    /**
     * Lista de reservas realizadas por el usuario
     */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reserva> reservas = new ArrayList<>();
    
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
    public List<Evento> getEventos() { return eventos; }
    public List<Reserva> getReservas() { return reservas; }
    
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
    public void setEventos(List<Evento> eventos) { this.eventos = eventos; }
    public void setReservas(List<Reserva> reservas) { this.reservas = reservas; }
    
    /**
     * Constructor para crear usuario desde JWT de Azure B2C
     */
    public Usuario(String email, String nombreAzure, String apellidoAzure, String azureB2cId, String rolAzure, String rutProductor) {
        this.email = email;
        this.nombreAzure = nombreAzure;
        this.apellidoAzure = apellidoAzure;
        this.azureB2cId = azureB2cId;
        this.rolAzure = rolAzure;
        this.rutProductor = rutProductor;
        this.esUsuarioAzure = true;
        this.fechaUltimaConexion = LocalDateTime.now();
        
        // Construir nombre completo
        this.nombre = (nombreAzure != null ? nombreAzure : "") + 
                     (apellidoAzure != null ? " " + apellidoAzure : "").trim();
        
        // Mapear rol de Azure B2C a TipoUsuario
        this.tipoUsuario = mapearRolAzure(rolAzure);
    }
    
    /**
     * Mapea el rol de Azure B2C a TipoUsuario
     */
    private TipoUsuario mapearRolAzure(String rolAzure) {
        if (rolAzure == null) {
            return TipoUsuario.CLIENTE; // Rol por defecto
        }
        
        String rolUpper = rolAzure.toUpperCase();
        
        switch (rolUpper) {
            case "ADMINISTRADOR":
                return TipoUsuario.ADMINISTRADOR;
            case "PRODUCTOR":
                return TipoUsuario.PRODUCTOR;
            case "CLIENTE":
                return TipoUsuario.CLIENTE;
            default:
                return TipoUsuario.CLIENTE;
        }
    }
    
    /**
     * Actualiza los datos del usuario desde Azure B2C
     */
    public void actualizarDesdeAzureB2C(String nombreAzure, String apellidoAzure, String rolAzure, String rutProductor) {
        this.nombreAzure = nombreAzure;
        this.apellidoAzure = apellidoAzure;
        this.rolAzure = rolAzure;
        this.rutProductor = rutProductor;
        
        // Actualizar nombre completo
        this.nombre = (nombreAzure != null ? nombreAzure : "") + 
                     (apellidoAzure != null ? " " + apellidoAzure : "").trim();
        
        // Actualizar tipo de usuario
        this.tipoUsuario = mapearRolAzure(rolAzure);
    }
    
    /**
     * Actualiza la fecha de última conexión
     */
    public void actualizarUltimaConexion() {
        this.fechaUltimaConexion = LocalDateTime.now();
    }
    
    /**
     * Agrega una reserva al usuario y establece la relación bidireccional
     */
    public void addReserva(Reserva reserva) {
        reservas.add(reserva);
        reserva.setUsuario(this);
    }
    
    /**
     * Remueve una reserva del usuario y limpia la relación
     */
    public void removeReserva(Reserva reserva) {
        reservas.remove(reserva);
        reserva.setUsuario(null);
    }
    
    /**
     * Agrega un evento al usuario y establece la relación bidireccional
     */
    public void addEvento(Evento evento) {
        eventos.add(evento);
        evento.setUsuario(this);
    }
    
    /**
     * Remueve un evento del usuario y limpia la relación
     */
    public void removeEvento(Evento evento) {
        eventos.remove(evento);
        evento.setUsuario(null);
    }
    
    /**
     * Verifica si el usuario es productor
     */
    public boolean isProductor() {
        return tipoUsuario == TipoUsuario.PRODUCTOR;
    }
    
    /**
     * Verifica si el usuario es cliente
     */
    public boolean isCliente() {
        return tipoUsuario == TipoUsuario.CLIENTE;
    }
    
    /**
     * Verifica si el usuario es administrador
     */
    public boolean isAdministrador() {
        return tipoUsuario == TipoUsuario.ADMINISTRADOR;
    }
    
    // Getters heredados de BaseEntity - agregados manualmente
    public Integer getActivo() { return this.activo; }
    public LocalDateTime getFechaCreacion() { return this.fechaCreacion; }
    
    // Setters heredados de BaseEntity - agregados manualmente
    public void setActivo(Integer activo) { this.activo = activo; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
} 