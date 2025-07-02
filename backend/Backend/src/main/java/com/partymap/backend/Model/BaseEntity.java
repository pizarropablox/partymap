package com.partymap.backend.Model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

/**
 * Clase base abstracta que proporciona funcionalidad común para todas las entidades.
 * Incluye auditoría automática (fecha de creación) y control de estado activo.
 * Esta clase es heredada por todas las entidades del sistema para mantener consistencia
 * en el manejo de metadatos y auditoría.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    
    /**
     * Indica si la entidad está activa en el sistema.
     * Permite soft delete sin eliminar físicamente los registros.
     */
    @Column(name = "activo", nullable = false)
    protected Integer activo = 1;
    
    /**
     * Fecha y hora de creación del registro.
     * Se establece automáticamente al crear la entidad y no se puede modificar.
     */
    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    protected LocalDateTime fechaCreacion;
    
    /**
     * Método que se ejecuta automáticamente antes de persistir una nueva entidad.
     * Establece la fecha de creación y asegura que el estado activo sea 1.
     */
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (activo == null) {
            activo = 1;
        }
    }
} 