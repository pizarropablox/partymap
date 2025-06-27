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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una ubicación geográfica en el sistema PartyMap.
 * Almacena información de dirección, comuna y coordenadas geográficas (latitud/longitud).
 * Las ubicaciones son utilizadas por los eventos para indicar dónde se realizarán.
 */
@Entity
@Table(name = "UBICACION")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Ubicacion extends BaseEntity {
    
    /**
     * Identificador único de la ubicación
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Dirección específica del lugar (calle, número, etc.)
     */
    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 150, message = "La dirección debe tener entre 5 y 150 caracteres")
    @Column(name = "direccion", length = 150, nullable = false)
    private String direccion;
    
    /**
     * Comuna o distrito donde se encuentra la ubicación
     */
    @NotBlank(message = "La comuna es obligatoria")
    @Size(min = 2, max = 50, message = "La comuna debe tener entre 2 y 50 caracteres")
    @Column(name = "comuna", length = 50, nullable = false)
    private String comuna;
    
    /**
     * Coordenada de latitud (entre -90 y 90 grados)
     */
    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    @Column(name = "latitud", precision = 10, scale = 8, nullable = false)
    private BigDecimal latitud;
    
    /**
     * Coordenada de longitud (entre -180 y 180 grados)
     */
    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    @Column(name = "longitud", precision = 11, scale = 8, nullable = false)
    private BigDecimal longitud;
    
    /**
     * Lista de eventos que se realizan en esta ubicación
     */
    @OneToMany(mappedBy = "ubicacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Evento> eventos = new ArrayList<>();
    
    // Getters y setters explícitos para evitar problemas con Lombok
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }
    
    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal latitud) { this.latitud = latitud; }
    
    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal longitud) { this.longitud = longitud; }
    
    public List<Evento> getEventos() {
        return eventos;
    }
    
    public void setEventos(List<Evento> eventos) {
        this.eventos = eventos;
    }
    
    // Getters heredados de BaseEntity - agregados manualmente
    public Integer getActivo() { return this.activo; }
    public LocalDateTime getFechaCreacion() { return this.fechaCreacion; }
    
    // Setters heredados de BaseEntity - agregados manualmente
    public void setActivo(Integer activo) { this.activo = activo; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    
    /**
     * Agrega un evento a la lista de la ubicación y establece la relación bidireccional
     */
    public void addEvento(Evento evento) {
        eventos.add(evento);
        evento.setUbicacion(this);
    }
    
    /**
     * Remueve un evento de la lista de la ubicación y limpia la relación
     */
    public void removeEvento(Evento evento) {
        eventos.remove(evento);
        evento.setUbicacion(null);
    }
    
    /**
     * Obtiene la dirección completa combinando dirección y comuna
     */
    public String getDireccionCompleta() {
        return direccion + ", " + comuna;
    }
    
    /**
     * Valida que las coordenadas estén dentro de rangos válidos
     * Latitud: -90 a 90, Longitud: -180 a 180
     */
    public boolean coordenadasValidas() {
        return latitud != null && longitud != null &&
               latitud.compareTo(BigDecimal.valueOf(-90)) >= 0 &&
               latitud.compareTo(BigDecimal.valueOf(90)) <= 0 &&
               longitud.compareTo(BigDecimal.valueOf(-180)) >= 0 &&
               longitud.compareTo(BigDecimal.valueOf(180)) <= 0;
    }
} 