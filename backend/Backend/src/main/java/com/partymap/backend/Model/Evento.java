package com.partymap.backend.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Evento", indexes = {
    @Index(name = "idx_evento_fecha", columnList = "fecha"),
    @Index(name = "idx_evento_estado", columnList = "estado"),
    @Index(name = "idx_evento_productor", columnList = "productor_id"),
    @Index(name = "idx_evento_ubicacion", columnList = "ubicacion_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Evento extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre del evento es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre del evento debe tener entre 3 y 100 caracteres")
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;
    
    @NotBlank(message = "La descripción del evento es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    @Column(name = "descripcion", columnDefinition = "CLOB", nullable = false)
    private String descripcion;
    
    @NotNull(message = "La fecha del evento es obligatoria")
    @Future(message = "La fecha del evento debe ser futura")
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;
    
    @NotNull(message = "El estado del evento es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoEvento estado = EstadoEvento.BORRADOR;
    
    @Column(name = "capacidad_maxima")
    private Integer capacidadMaxima;
    
    @Column(name = "precio_entrada", precision = 10, scale = 2)
    private BigDecimal precioEntrada;
    
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;
    
    // Relación con Ubicacion
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "La ubicación es obligatoria")
    private Ubicacion ubicacion;
    
    // Relación con Productor
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "productor_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "El productor es obligatorio")
    private Productor productor;
    
    // Relación con Reservas
    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reserva> reservas = new ArrayList<>();
    
    // Métodos de conveniencia
    public void addReserva(Reserva reserva) {
        reservas.add(reserva);
        reserva.setEvento(this);
    }
    
    public void removeReserva(Reserva reserva) {
        reservas.remove(reserva);
        reserva.setEvento(null);
    }
    
    // Métodos de negocio
    public boolean isDisponible() {
        return EstadoEvento.PUBLICADO.equals(estado) && 
               (capacidadMaxima == null || getCantidadReservas() < capacidadMaxima);
    }
    
    public int getCantidadReservas() {
        return reservas.stream()
                .mapToInt(Reserva::getCantidad)
                .sum();
    }
    
    public int getCuposDisponibles() {
        if (capacidadMaxima == null) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, capacidadMaxima - getCantidadReservas());
    }
    
    public boolean isEventoPasado() {
        return fecha.isBefore(LocalDateTime.now());
    }
    
    public boolean isEventoProximo() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime proximo = ahora.plusDays(7);
        return fecha.isAfter(ahora) && fecha.isBefore(proximo);
    }
    
    public void publicar() {
        if (EstadoEvento.BORRADOR.equals(estado)) {
            this.estado = EstadoEvento.PUBLICADO;
        }
    }
    
    public void cancelar() {
        this.estado = EstadoEvento.CANCELADO;
    }
    
    public void finalizar() {
        this.estado = EstadoEvento.FINALIZADO;
    }
} 