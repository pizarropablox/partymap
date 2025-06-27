package com.partymap.backend.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una reserva de un usuario para un evento específico.
 * Una reserva conecta a un usuario con un evento, especificando la cantidad de entradas
 * y el estado de la reserva. Incluye información de precios y comentarios opcionales.
 */
@Entity
@Table(name = "RESERVA")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Reserva extends BaseEntity {
    
    /**
     * Identificador único de la reserva
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Número de entradas reservadas
     */
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
    
    /**
     * Fecha y hora en que se realizó la reserva
     */
    @NotNull(message = "La fecha de reserva es obligatoria")
    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;
    
    /**
     * Precio por entrada individual
     */
    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;
    
    /**
     * Precio total de la reserva (precio unitario * cantidad)
     */
    @Column(name = "precio_total", precision = 10, scale = 2)
    private BigDecimal precioTotal;
    
    /**
     * Comentarios adicionales de la reserva
     */
    @Column(name = "comentarios", length = 500)
    private String comentarios;
    
    /**
     * Estado actual de la reserva
     */
    @NotNull(message = "El estado de la reserva es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoReserva estado = EstadoReserva.RESERVADA;
    
    /**
     * Usuario que realizó la reserva
     */
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "El usuario es obligatorio")
    private Usuario usuario;
    
    /**
     * Evento para el cual se realizó la reserva
     */
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "El evento es obligatorio")
    private Evento evento;
    
    // Getters y setters explícitos para evitar problemas con Lombok
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    
    public LocalDateTime getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(LocalDateTime fechaReserva) { this.fechaReserva = fechaReserva; }
    
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    
    public BigDecimal getPrecioTotal() { return precioTotal; }
    public void setPrecioTotal(BigDecimal precioTotal) { this.precioTotal = precioTotal; }
    
    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }
    
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
    
    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    // Getters heredados de BaseEntity - agregados manualmente
    public Integer getActivo() { return this.activo; }
    public LocalDateTime getFechaCreacion() { return this.fechaCreacion; }
    
    // Setters heredados de BaseEntity - agregados manualmente
    public void setActivo(Integer activo) { this.activo = activo; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    /**
     * Cancela la reserva cambiando su estado a CANCELADA
     */
    public void cancelar() {
        this.estado = EstadoReserva.CANCELADA;
    }
    
    /**
     * Verifica si la reserva está activa (estado RESERVADA)
     */
    public boolean isActiva() {
        return EstadoReserva.RESERVADA.equals(estado);
    }
    
    /**
     * Verifica si la reserva está cancelada
     */
    public boolean isCancelada() {
        return EstadoReserva.CANCELADA.equals(estado);
    }
    
    /**
     * Calcula el precio total de la reserva
     * Multiplica el precio unitario por la cantidad
     */
    public BigDecimal calcularPrecioTotal() {
        if (precioUnitario != null && cantidad != null) {
            return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Actualiza el precio total de la reserva
     */
    public void actualizarPrecioTotal() {
        this.precioTotal = calcularPrecioTotal();
    }
    
    /**
     * Método que se ejecuta antes de persistir una nueva reserva
     * Establece valores por defecto y calcula el precio total
     */
    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (fechaReserva == null) {
            fechaReserva = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoReserva.RESERVADA;
        }
        actualizarPrecioTotal();
    }
} 