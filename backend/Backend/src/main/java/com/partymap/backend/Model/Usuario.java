package com.partymap.backend.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Usuario", indexes = {
    @Index(name = "idx_usuario_email", columnList = "email"),
    @Index(name = "idx_usuario_tipo", columnList = "tipo_usuario")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Usuario extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    @Column(name = "contraseña", length = 100, nullable = false)
    private String contraseña;
    
    @NotNull(message = "El tipo de usuario es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", length = 20, nullable = false)
    private TipoUsuario tipoUsuario;
    
    // Relaciones - usando FetchType.LAZY para mejor rendimiento
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Productor productor;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reserva> reservas = new ArrayList<>();
    
    // Métodos de conveniencia
    public void addReserva(Reserva reserva) {
        reservas.add(reserva);
        reserva.setUsuario(this);
    }
    
    public void removeReserva(Reserva reserva) {
        reservas.remove(reserva);
        reserva.setUsuario(null);
    }
    
    public boolean isProductor() {
        return TipoUsuario.PRODUCTOR.equals(tipoUsuario);
    }
    
    public boolean isCliente() {
        return TipoUsuario.CLIENTE.equals(tipoUsuario);
    }
    
    public boolean isAdministrador() {
        return TipoUsuario.ADMINISTRADOR.equals(tipoUsuario);
    }
} 