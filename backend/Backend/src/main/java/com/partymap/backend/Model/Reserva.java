package com.partymap.backend.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Reserva", indexes = {
    @Index(name = "idx_reserva_usuario", columnList = "usuario_id"),
    @Index(name = "idx_reserva_evento", columnList = "evento_id"),
    @Index(name = "idx_reserva_fecha", columnList = "fecha_reserva"),
    @Index(name = "idx_reserva_estado", columnList = "estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Reserva extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
    
    @NotNull(message = "La fecha de reserva es obligatoria")
    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;
    
    @NotNull(message = "El estado de la reserva es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;
    
    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;
    
    @Column(name = "precio_total", precision = 10, scale = 2)
    private BigDecimal precioTotal;
    
    @Column(name = "comentarios", length = 500)
    private String comentarios;
    
    // Relación con Usuario
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "El usuario es obligatorio")
    private Usuario usuario;
    
    // Relación con Evento
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "El evento es obligatorio")
    private Evento evento;
    
    // Métodos de negocio
    public void confirmar() {
        if (EstadoReserva.PENDIENTE.equals(estado)) {
            this.estado = EstadoReserva.CONFIRMADA;
        }
    }
    
    public void cancelar() {
        if (!EstadoReserva.COMPLETADA.equals(estado)) {
            this.estado = EstadoReserva.CANCELADA;
        }
    }
    
    public void completar() {
        if (EstadoReserva.CONFIRMADA.equals(estado)) {
            this.estado = EstadoReserva.COMPLETADA;
        }
    }
    
    public boolean isActiva() {
        return EstadoReserva.PENDIENTE.equals(estado) || 
               EstadoReserva.CONFIRMADA.equals(estado);
    }
    
    public boolean isCancelada() {
        return EstadoReserva.CANCELADA.equals(estado);
    }
    
    public boolean isCompletada() {
        return EstadoReserva.COMPLETADA.equals(estado);
    }
    
    public BigDecimal calcularPrecioTotal() {
        if (precioUnitario != null && cantidad != null) {
            return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        }
        return BigDecimal.ZERO;
    }
    
    public void actualizarPrecioTotal() {
        this.precioTotal = calcularPrecioTotal();
    }
    
    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (fechaReserva == null) {
            fechaReserva = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoReserva.PENDIENTE;
        }
        actualizarPrecioTotal();
    }
    
    @PreUpdate
    @Override
    protected void onUpdate() {
        super.onUpdate();
        actualizarPrecioTotal();
    }
} 