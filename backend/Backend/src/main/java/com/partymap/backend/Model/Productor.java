package com.partymap.backend.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa a un productor de eventos en el sistema PartyMap.
 * Un productor es un usuario especializado que puede crear y gestionar eventos.
 * Cada productor tiene información empresarial (nombre de empresa, RUT) y está
 * asociado a un usuario del sistema.
 */
@Entity
@Table(name = "PRODUCTOR", indexes = {
    @Index(name = "idx_productor_rut", columnList = "rut"),
    @Index(name = "idx_productor_usuario", columnList = "usuario_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Productor extends BaseEntity {
    
    /**
     * Identificador único del productor
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Nombre de la empresa o organización del productor
     */
    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre de la empresa debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre_empresa", length = 100, nullable = false)
    private String nombreEmpresa;
    
    /**
     * RUT único de la empresa (formato chileno)
     */
    @NotBlank(message = "El RUT es obligatorio")
    @Pattern(regexp = "^[0-9]{7,8}-[0-9kK]$", message = "El formato del RUT no es válido")
    @Column(name = "rut", length = 20, unique = true, nullable = false)
    private String rut;
    
    /**
     * Usuario asociado al productor (relación uno a uno)
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false, unique = true)
    @NotNull(message = "El usuario es obligatorio")
    private Usuario usuario;
    
    /**
     * Lista de eventos creados por el productor
     */
    @OneToMany(mappedBy = "productor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Evento> eventos = new ArrayList<>();
    
    /**
     * Agrega un evento a la lista del productor y establece la relación bidireccional
     */
    public void addEvento(Evento evento) {
        eventos.add(evento);
        evento.setProductor(this);
    }
    
    /**
     * Remueve un evento de la lista del productor y limpia la relación
     */
    public void removeEvento(Evento evento) {
        eventos.remove(evento);
        evento.setProductor(null);
    }
    
    /**
     * Valida que el RUT tenga un formato válido chileno
     * Verifica que tenga entre 7-8 dígitos + dígito verificador
     */
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