package com.partymap.backend.Controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.partymap.backend.DTO.UbicacionDTO;
import com.partymap.backend.DTO.UbicacionResponseDTO;
import com.partymap.backend.Exceptions.NotFoundException;
import com.partymap.backend.Model.Ubicacion;
import com.partymap.backend.Service.UbicacionService;

/**
 * Controlador REST para la gestión de ubicaciones.
 * Proporciona endpoints para operaciones CRUD de ubicaciones y funcionalidades adicionales.
 */
@RestController
@CrossOrigin
@RequestMapping("/ubicacion")
public class UbicacionController {

    private final UbicacionService ubicacionService;

    public UbicacionController(UbicacionService ubicacionService) {
        this.ubicacionService = ubicacionService;
    }

    /**
     * Obtiene todas las ubicaciones del sistema
     * GET /ubicacion
     */
    @GetMapping("/all")
    public ResponseEntity<List<UbicacionResponseDTO>> getAllUbicaciones() {
        List<Ubicacion> ubicaciones = ubicacionService.getAllUbicaciones();
        List<UbicacionResponseDTO> ubicacionesDTO = ubicaciones.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ubicacionesDTO);
    }

    /**
     * Obtiene una ubicación por su ID
     * GET /ubicacion/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UbicacionResponseDTO> getUbicacionById(@PathVariable Long id) {
        Optional<Ubicacion> ubicacion = ubicacionService.getUbicacionById(id);
        if (ubicacion.isPresent()) {
            return ResponseEntity.ok(convertToResponseDTO(ubicacion.get()));
        } else {
            throw new NotFoundException("Ubicación no encontrada con ID: " + id);
        }
    }

    /**
     * Crea una nueva ubicación
     * POST /ubicacion
     */
    @PostMapping
    public ResponseEntity<UbicacionResponseDTO> createUbicacion(@RequestBody UbicacionDTO ubicacionDTO) {
        try {
            Ubicacion ubicacion = convertToEntity(ubicacionDTO);
            Ubicacion ubicacionCreada = ubicacionService.createUbicacion(ubicacion);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(convertToResponseDTO(ubicacionCreada));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualiza una ubicación existente
     * PUT /ubicacion/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UbicacionResponseDTO> updateUbicacion(
            @PathVariable Long id,
            @RequestBody UbicacionDTO ubicacionDTO) {
        try {
            Ubicacion ubicacion = convertToEntity(ubicacionDTO);
            Ubicacion ubicacionActualizada = ubicacionService.updateUbicacion(id, ubicacion);
            return ResponseEntity.ok(convertToResponseDTO(ubicacionActualizada));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Elimina una ubicación
     * DELETE /ubicacion/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUbicacion(@PathVariable Long id) {
        try {
            Optional<Ubicacion> ubicacion = ubicacionService.getUbicacionById(id);
            if (ubicacion.isPresent()) {
                ubicacionService.deleteUbicacion(ubicacion.get());
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca ubicaciones por comuna
     * GET /ubicacion/buscar?comuna=Providencia
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<UbicacionResponseDTO>> buscarUbicaciones(
            @RequestParam(required = false) String comuna,
            @RequestParam(required = false) String direccion) {
        
        List<Ubicacion> ubicaciones = ubicacionService.getAllUbicaciones();
        
        // Aplicar filtros
        List<Ubicacion> ubicacionesFiltradas = ubicaciones.stream()
                .filter(ubicacion -> comuna == null || 
                    ubicacion.getComuna().toLowerCase().contains(comuna.toLowerCase()))
                .filter(ubicacion -> direccion == null || 
                    ubicacion.getDireccion().toLowerCase().contains(direccion.toLowerCase()))
                .collect(Collectors.toList());
        
        List<UbicacionResponseDTO> ubicacionesDTO = ubicacionesFiltradas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ubicacionesDTO);
    }

    /**
     * Obtiene ubicaciones por comuna específica
     * GET /ubicacion/comuna/{comuna}
     */
    @GetMapping("/comuna/{comuna}")
    public ResponseEntity<List<UbicacionResponseDTO>> getUbicacionesByComuna(@PathVariable String comuna) {
        List<Ubicacion> ubicaciones = ubicacionService.getUbicacionesByComuna(comuna);
        List<UbicacionResponseDTO> ubicacionesDTO = ubicaciones.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ubicacionesDTO);
    }

    /**
     * Valida si las coordenadas son válidas
     * GET /ubicacion/validar-coordenadas?latitud=-33.4489&longitud=-70.6693
     */
    @GetMapping("/validar-coordenadas")
    public ResponseEntity<Boolean> validarCoordenadas(
            @RequestParam Double latitud,
            @RequestParam Double longitud) {
        boolean coordenadasValidas = ubicacionService.validarCoordenadas(latitud, longitud);
        return ResponseEntity.ok(coordenadasValidas);
    }

    // Métodos de conversión privados

    /**
     * Convierte un Ubicacion a UbicacionResponseDTO
     */
    private UbicacionResponseDTO convertToResponseDTO(Ubicacion ubicacion) {
        UbicacionResponseDTO dto = new UbicacionResponseDTO();
        dto.setId(ubicacion.getId());
        dto.setDireccion(ubicacion.getDireccion());
        dto.setComuna(ubicacion.getComuna());
        dto.setLatitud(ubicacion.getLatitud());
        dto.setLongitud(ubicacion.getLongitud());
        dto.setActivo(ubicacion.getActivo());
        dto.setFechaCreacion(ubicacion.getFechaCreacion());
        dto.setFechaModificacion(ubicacion.getFechaModificacion());
        return dto;
    }

    /**
     * Convierte un UbicacionDTO a Ubicacion
     */
    private Ubicacion convertToEntity(UbicacionDTO dto) {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setId(dto.getId());
        ubicacion.setDireccion(dto.getDireccion());
        ubicacion.setComuna(dto.getComuna());
        ubicacion.setLatitud(dto.getLatitud());
        ubicacion.setLongitud(dto.getLongitud());
        return ubicacion;
    }
} 