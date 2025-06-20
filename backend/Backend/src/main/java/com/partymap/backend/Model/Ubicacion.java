package com.partymap.backend.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Ubicacion", indexes = {
    @Index(name = "idx_ubicacion_comuna", columnList = "comuna"),
    @Index(name = "idx_ubicacion_coords", columnList = "latitud,longitud")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Ubicacion extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 150, message = "La dirección debe tener entre 5 y 150 caracteres")
    @Column(name = "direccion", length = 150, nullable = false)
    private String direccion;
    
    @NotBlank(message = "La comuna es obligatoria")
    @Size(min = 2, max = 50, message = "La comuna debe tener entre 2 y 50 caracteres")
    @Column(name = "comuna", length = 50, nullable = false)
    private String comuna;
    
    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    @Column(name = "latitud", precision = 10, scale = 8, nullable = false)
    private BigDecimal latitud;
    
    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    @Column(name = "longitud", precision = 11, scale = 8, nullable = false)
    private BigDecimal longitud;
    
    // Relación con Eventos
    @OneToMany(mappedBy = "ubicacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Evento> eventos = new ArrayList<>();
    
    // Métodos de conveniencia
    public void addEvento(Evento evento) {
        eventos.add(evento);
        evento.setUbicacion(this);
    }
    
    public void removeEvento(Evento evento) {
        eventos.remove(evento);
        evento.setUbicacion(null);
    }
    
    // Método para obtener dirección completa
    public String getDireccionCompleta() {
        return direccion + ", " + comuna;
    }
    
    // Método para validar coordenadas
    public boolean coordenadasValidas() {
        return latitud != null && longitud != null &&
               latitud.compareTo(BigDecimal.valueOf(-90)) >= 0 &&
               latitud.compareTo(BigDecimal.valueOf(90)) <= 0 &&
               longitud.compareTo(BigDecimal.valueOf(-180)) >= 0 &&
               longitud.compareTo(BigDecimal.valueOf(180)) <= 0;
    }
} 