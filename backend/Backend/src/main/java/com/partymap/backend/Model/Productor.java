package com.partymap.backend.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Productor", indexes = {
    @Index(name = "idx_productor_rut", columnList = "rut"),
    @Index(name = "idx_productor_usuario", columnList = "usuario_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Productor extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre de la empresa debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre_empresa", length = 100, nullable = false)
    private String nombreEmpresa;
    
    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^[0-9]{7,8}-[0-9kK]$", message = "El formato del RUT no es válido")
    @Column(name = "rut", length = 20, unique = true, nullable = false)
    private String rut;
    
    // Relación con Usuario - usando FetchType.LAZY para mejor rendimiento
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false, unique = true)
    @NotNull(message = "El usuario es obligatorio")
    private Usuario usuario;
    
    // Relación con Eventos
    @OneToMany(mappedBy = "productor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Evento> eventos = new ArrayList<>();
    
    // Métodos de conveniencia
    public void addEvento(Evento evento) {
        eventos.add(evento);
        evento.setProductor(this);
    }
    
    public void removeEvento(Evento evento) {
        eventos.remove(evento);
        evento.setProductor(null);
    }
    
    // Método para validar RUT chileno
    public boolean isValidRut() {
        if (rut == null || rut.trim().isEmpty()) {
            return false;
        }
        
        String rutLimpio = rut.replaceAll("[.-]", "");
        if (rutLimpio.length() < 2) {
            return false;
        }
        
        String dv = rutLimpio.substring(rutLimpio.length() - 1);
        String numero = rutLimpio.substring(0, rutLimpio.length() - 1);
        
        try {
            int num = Integer.parseInt(numero);
            if (num < 1000000) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        
        return true;
    }
} 