package com.partymap.backend.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO genérico para respuestas paginadas.
 * Contiene la lista de elementos, información de paginación y metadatos.
 * 
 * USO:
 * - Responder consultas de listas grandes (eventos, reservas, usuarios, etc.)
 * - Incluye información de página actual, total de elementos y navegación
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponseDTO<T> {
    
    /**
     * Lista de elementos de la página actual
     */
    private List<T> contenido;
    
    /**
     * Número de la página actual
     */
    private int pagina;
    
    /**
     * Tamaño de la página (cantidad de elementos por página)
     */
    private int tamanio;
    
    /**
     * Total de elementos en la consulta
     */
    private long totalElementos;
    
    /**
     * Total de páginas disponibles
     */
    private int totalPaginas;
    
    /**
     * Indica si es la primera página
     */
    private boolean primera;
    
    /**
     * Indica si es la última página
     */
    private boolean ultima;
    
    /**
     * Indica si hay una página siguiente
     */
    private boolean tieneSiguiente;
    
    /**
     * Indica si hay una página anterior
     */
    private boolean tieneAnterior;
} 