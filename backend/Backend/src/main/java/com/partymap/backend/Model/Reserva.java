package com.partymap.backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Reserva")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cantidad")
    private Integer cantidad;
    
    @Column(name = "fecha_reserva")
    private LocalDateTime fechaReserva;
    
    // Relación con Usuario
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;
    
    // Relación con Evento
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "evento_id", referencedColumnName = "id")
    private Evento evento;
} 