package com.partymap.backend.Model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

/**
 * Clase base abstracta que proporciona funcionalidad común para todas las entidades.
 * Incluye auditoría automática (fechas de creación/modificación) y control de estado activo.
 * Esta clase es heredada por todas las entidades del sistema para mantener consistencia
 * en el manejo de metadatos y auditoría.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class BaseEntity {
    
    /**
     * Indica si la entidad está activa en el sistema.
     * Permite soft delete sin eliminar físicamente los registros.
     */
    @Column(name = "activo", nullable = false)
    private Integer activo = 1;
    
    /**
     * Fecha y hora de creación del registro.
     * Se establece automáticamente al crear la entidad y no se puede modificar.
     */
    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    /**
     * Fecha y hora de la última modificación del registro.
     * Se actualiza automáticamente cada vez que se modifica la entidad.
     */
    @LastModifiedDate
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
    
    /**
     * Método que se ejecuta automáticamente antes de persistir una nueva entidad.
     * Establece las fechas de creación y modificación, y asegura que el estado activo sea 1.
     */
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
        if (activo == null) {
            activo = 1;
        }
    }
    
    /**
     * Método que se ejecuta automáticamente antes de actualizar una entidad existente.
     * Actualiza la fecha de modificación con la fecha y hora actual.
     */
    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
} 