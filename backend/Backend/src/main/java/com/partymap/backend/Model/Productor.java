package com.partymap.backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Productor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Productor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre_empresa", length = 100)
    private String nombreEmpresa;
    
    @Column(name = "rut", length = 20)
    private String rut;
    
    // Relación con Usuario
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;
    
    // Relación con Eventos
    @OneToMany(mappedBy = "productor", cascade = CascadeType.ALL)
    private List<Evento> eventos;
} 