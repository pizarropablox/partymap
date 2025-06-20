package com.partymap.backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Evento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre", length = 100)
    private String nombre;
    
    @Column(name = "descripcion", columnDefinition = "CLOB")
    private String descripcion;
    
    @Column(name = "fecha")
    private LocalDateTime fecha;
    
    // Relación con Ubicacion
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ubicacion_id", referencedColumnName = "id")
    private Ubicacion ubicacion;
    
    // Relación con Productor
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "productor_id", referencedColumnName = "id")
    private Productor productor;
    
    // Relación con Reservas
    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL)
    private List<Reserva> reservas;
} 