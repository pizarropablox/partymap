package com.partymap.backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre", length = 100)
    private String nombre;
    
    @Column(name = "email", length = 100, unique = true)
    private String email;
    
    @Column(name = "contraseña", length = 100)
    private String contraseña;
    
    @Column(name = "tipo_usuario", length = 50)
    private String tipoUsuario;
    
    // Relaciones
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Productor productor;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Reserva> reservas;
} 