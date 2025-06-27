package com.partymap.backend.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Evento.
 * Contiene información completa del evento incluyendo ubicación y productor.
 * 
 * USO:
 * - Respuesta de consultas de evento (GET)
 * - Incluye metadatos de auditoría y estado activo
 * - Incluye información de ubicación y productor relacionados
 * - Incluye campos calculados como cupos disponibles y disponibilidad
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoResponseDTO {
    
    /**
     * Identificador único del evento
     */
    private Long id;
    
    /**
     * Nombre o título del evento
     */
    private String nombre;
    
    /**
     * Descripción detallada del evento
     */
    private String descripcion;
    
    /**
     * Fecha y hora programada para el evento
     */
    private LocalDateTime fecha;
    
    /**
     * Número máximo de personas que pueden asistir al evento
     */
    private Integer capacidadMaxima;
    
    /**
     * Precio de entrada al evento
     */
    private BigDecimal precioEntrada;
    
    /**
     * URL de la imagen promocional del evento
     */
    private String imagenUrl;
    
    /**
     * Indica si el evento está activo
     */
    private Integer activo;
    
    /**
     * Fecha de creación del evento
     */
    private LocalDateTime fechaCreacion;
    
    /**
     * Información de la ubicación relacionada
     */
    private UbicacionResponseDTO ubicacion;
    
    /**
     * Información del productor relacionado
     */
    private ProductorResponseDTO productor;
    
    /**
     * Cupos disponibles para el evento
     */
    private Integer cuposDisponibles;
    
    /**
     * Indica si el evento está disponible para reservas
     */
    private Boolean disponible;
    
    /**
     * Indica si el evento ya pasó
     */
    private Boolean eventoPasado;
    
    /**
     * Indica si el evento está próximo (en los próximos 7 días)
     */
    private Boolean eventoProximo;
    
    // Getters manuales para asegurar compatibilidad
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public LocalDateTime getFecha() { return fecha; }
    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public BigDecimal getPrecioEntrada() { return precioEntrada; }
    public String getImagenUrl() { return imagenUrl; }
    public Integer getActivo() { return activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public UbicacionResponseDTO getUbicacion() { return ubicacion; }
    public ProductorResponseDTO getProductor() { return productor; }
    public Integer getCuposDisponibles() { return cuposDisponibles; }
    public Boolean getDisponible() { return disponible; }
    public Boolean getEventoPasado() { return eventoPasado; }
    public Boolean getEventoProximo() { return eventoProximo; }
    
    // Setters manuales para asegurar compatibilidad
    public void setId(Long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void setCapacidadMaxima(Integer capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
    public void setPrecioEntrada(BigDecimal precioEntrada) { this.precioEntrada = precioEntrada; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public void setActivo(Integer activo) { this.activo = activo; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setUbicacion(UbicacionResponseDTO ubicacion) { this.ubicacion = ubicacion; }
    public void setProductor(ProductorResponseDTO productor) { this.productor = productor; }
    public void setCuposDisponibles(Integer cuposDisponibles) { this.cuposDisponibles = cuposDisponibles; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }
    public void setEventoPasado(Boolean eventoPasado) { this.eventoPasado = eventoPasado; }
    public void setEventoProximo(Boolean eventoProximo) { this.eventoProximo = eventoProximo; }
} 