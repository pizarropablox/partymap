package com.partymap.backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Ubicacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ubicacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "direccion", length = 150)
    private String direccion;
    
    @Column(name = "comuna", length = 50)
    private String comuna;
    
    @Column(name = "latitud")
    private Float latitud;
    
    @Column(name = "longitud")
    private Float longitud;
    
    // Relación con Eventos
    @OneToMany(mappedBy = "ubicacion", cascade = CascadeType.ALL)
    private List<Evento> eventos;
} 