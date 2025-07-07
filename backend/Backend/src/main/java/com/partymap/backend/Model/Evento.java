package com.partymap.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad central que representa un evento en el sistema PartyMap.
 * Un evento es una actividad organizada por un usuario productor en una ubicación específica.
 * Los eventos pueden recibir reservas de los clientes y tienen capacidad máxima opcional.
 */
@Entity
@Table(name = "EVENTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Evento extends BaseEntity {
    
    /**
     * Identificador único del evento
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evento_seq")
    @SequenceGenerator(name = "evento_seq", sequenceName = "EVENTO_SEQ", allocationSize = 1)
    private Long id;
    
    /**
     * Nombre o título del evento
     */
    @NotBlank(message = "El nombre del evento es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre del evento debe tener entre 3 y 100 caracteres")
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;
    
    /**
     * Descripción detallada del evento
     */
    @NotBlank(message = "La descripción del evento es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    @Column(name = "descripcion", columnDefinition = "CLOB", nullable = false)
    private String descripcion;
    
    /**
     * Fecha y hora programada para el evento
     */
    @NotNull(message = "La fecha del evento es obligatoria")
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
    
    /**
     * Número máximo de personas que pueden asistir al evento
     */
    @Column(name = "capacidad_maxima")
    private Integer capacidadMaxima;
    
    /**
     * Precio de entrada al evento
     */
    @Column(name = "precio_entrada", precision = 10, scale = 2)
    private BigDecimal precioEntrada;
    
    /**
     * URL de la imagen promocional del evento
     */
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;
    
    /**
     * Ubicación donde se realizará el evento
     */
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "ubicacion_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "La ubicación es obligatoria")
    private Ubicacion ubicacion;
    
    /**
     * Usuario productor responsable de organizar el evento
     */
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "El usuario productor es obligatorio")
    private Usuario usuario;
    
    /**
     * Lista de reservas realizadas para este evento
     */
    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reserva> reservas = new ArrayList<>();
    
    // Getters y setters explícitos para evitar problemas con Lombok
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    
    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(Integer capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
    
    public BigDecimal getPrecioEntrada() { return precioEntrada; }
    public void setPrecioEntrada(BigDecimal precioEntrada) { this.precioEntrada = precioEntrada; }
    
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    
    public Ubicacion getUbicacion() { return ubicacion; }
    public void setUbicacion(Ubicacion ubicacion) { this.ubicacion = ubicacion; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    public List<Reserva> getReservas() { return reservas; }
    public void setReservas(List<Reserva> reservas) { this.reservas = reservas; }
    
    // Getters heredados de BaseEntity - agregados manualmente
    public Integer getActivo() { return this.activo; }
    public LocalDateTime getFechaCreacion() { return this.fechaCreacion; }
    
    // Setters heredados de BaseEntity - agregados manualmente
    public void setActivo(Integer activo) { this.activo = activo; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    /**
     * Agrega una reserva al evento y establece la relación bidireccional
     */
    public void addReserva(Reserva reserva) {
        reservas.add(reserva);
        reserva.setEvento(this);
    }
    
    /**
     * Remueve una reserva del evento y limpia la relación
     */
    public void removeReserva(Reserva reserva) {
        reservas.remove(reserva);
        reserva.setEvento(null);
    }
    
    /**
     * Verifica si el evento está disponible para nuevas reservas
     * (no ha pasado y tiene cupos disponibles)
     */
    public boolean isDisponible() {
        return !isEventoPasado() && 
               (capacidadMaxima == null || getCantidadReservasActivas() < capacidadMaxima);
    }
    
    /**
     * Calcula el total de reservas activas para el evento
     */
    public int getCantidadReservasActivas() {
        return reservas.stream()
                .filter(Reserva::isActiva)
                .mapToInt(Reserva::getCantidad)
                .sum();
    }
    
    /**
     * Calcula los cupos disponibles para el evento
     */
    public int getCuposDisponibles() {
        if (capacidadMaxima == null) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, capacidadMaxima - getCantidadReservasActivas());
    }
    
    /**
     * Verifica si el evento ya pasó
     */
    public boolean isEventoPasado() {
        return fecha.isBefore(LocalDateTime.now());
    }
    
    /**
     * Verifica si el evento es próximo (en las próximas 24 horas)
     */
    public boolean isEventoProximo() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime proximo = ahora.plusHours(24);
        return fecha.isAfter(ahora) && fecha.isBefore(proximo);
    }
} 