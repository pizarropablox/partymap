package com.partymap.backend.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa a un usuario en el sistema PartyMap.
 * Un usuario puede ser cliente, productor o administrador, cada uno con diferentes
 * permisos y funcionalidades. Los usuarios pueden hacer reservas y, si son productores,
 * pueden crear y gestionar eventos.
 */
@Entity
@Table(name = "USUARIO", indexes = {
    @Index(name = "idx_usuario_email", columnList = "email"),
    @Index(name = "idx_usuario_tipo", columnList = "tipo_usuario")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Usuario extends BaseEntity {
    
    /**
     * Identificador único del usuario
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Nombre completo del usuario
     */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;
    
    /**
     * Email único del usuario, usado para autenticación
     */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;
    
    /**
     * Contraseña encriptada del usuario
     */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    @Column(name = "contraseña", length = 100, nullable = false)
    private String contraseña;
    
    /**
     * Tipo de usuario que determina sus permisos en el sistema
     */
    @NotNull(message = "El tipo de usuario es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", length = 20, nullable = false)
    private TipoUsuario tipoUsuario;
    
    /**
     * Relación uno a uno con Productor (solo si el usuario es productor)
     */
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Productor productor;
    
    /**
     * Lista de reservas realizadas por el usuario
     */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reserva> reservas = new ArrayList<>();
    
    /**
     * Agrega una reserva a la lista del usuario y establece la relación bidireccional
     */
    public void addReserva(Reserva reserva) {
        reservas.add(reserva);
        reserva.setUsuario(this);
    }
    
    /**
     * Remueve una reserva de la lista del usuario y limpia la relación
     */
    public void removeReserva(Reserva reserva) {
        reservas.remove(reserva);
        reserva.setUsuario(null);
    }
    
    /**
     * Verifica si el usuario es un productor
     */
    public boolean isProductor() {
        return TipoUsuario.PRODUCTOR.equals(tipoUsuario);
    }
    
    /**
     * Verifica si el usuario es un cliente
     */
    public boolean isCliente() {
        return TipoUsuario.CLIENTE.equals(tipoUsuario);
    }
    
    /**
     * Verifica si el usuario es un administrador
     */
    public boolean isAdministrador() {
        return TipoUsuario.ADMINISTRADOR.equals(tipoUsuario);
    }
} 