package com.partymap.backend.Model;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad central que representa un evento en el sistema PartyMap.
 * Un evento es una actividad organizada por un productor en una ubicación específica.
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "ubicacion_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "La ubicación es obligatoria")
    private Ubicacion ubicacion;
    
    /**
     * Productor responsable de organizar el evento
     */
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "productor_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "El productor es obligatorio")
    private Productor productor;
    
    /**
     * Lista de reservas realizadas para este evento
     */
    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reserva> reservas = new ArrayList<>();
    
    // Setters explícitos para evitar problemas con Lombok
    public void setProductor(Productor productor) {
        this.productor = productor;
    }
    
    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }
    
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
     * Verifica si el evento está próximo (en los próximos 7 días)
     */
    public boolean isEventoProximo() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime proximo = ahora.plusDays(7);
        return fecha.isAfter(ahora) && fecha.isBefore(proximo);
    }
} 