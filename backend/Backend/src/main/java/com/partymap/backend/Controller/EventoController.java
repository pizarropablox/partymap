package com.partymap.backend.Controller;

import java.io.IOException;
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

import com.partymap.backend.DTO.EventoConUbicacionDTO;
import com.partymap.backend.DTO.EventoDTO;
import com.partymap.backend.DTO.EventoResponseDTO;
import com.partymap.backend.DTO.UbicacionDTO;
import com.partymap.backend.Exceptions.NotFoundException;
import com.partymap.backend.Model.Evento;
import com.partymap.backend.Model.Productor;
import com.partymap.backend.Model.Ubicacion;
import com.partymap.backend.Repository.ProductorRepository;
import com.partymap.backend.Repository.UbicacionRepository;
import com.partymap.backend.Repository.UsuarioRepository;
import com.partymap.backend.Service.EventoService;

/**
 * Controlador REST para gestionar eventos.
 * Proporciona endpoints para CRUD de eventos y operaciones especializadas.
 * 
 * ENDPOINTS:
 * - GET /evento - Obtener todos los eventos
 * - GET /evento/{id} - Obtener evento por ID
 * - POST /evento - Crear nuevo evento
 * - POST /evento/con-ubicacion - Crear evento con ubicación
 * - PUT /evento/{id} - Actualizar evento
 * - DELETE /evento/{id} - Eliminar evento
 * - GET /evento/buscar - Buscar eventos con filtros
 * - GET /evento/proximos - Obtener eventos próximos
 * - GET /evento/disponibles - Obtener eventos disponibles
 * - GET /evento/{id}/cupos - Obtener cupos disponibles
 */
@RestController
@CrossOrigin
@RequestMapping("/evento")
public class EventoController {

    private final EventoService eventoService;
    private final ProductorRepository productorRepository;
    private final UbicacionRepository ubicacionRepository;
    private final UsuarioRepository usuarioRepository;

    public EventoController(EventoService eventoService, 
                          ProductorRepository productorRepository,
                          UbicacionRepository ubicacionRepository,
                          UsuarioRepository usuarioRepository) {
        this.eventoService = eventoService;
        this.productorRepository = productorRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Obtiene todos los eventos del sistema
     * GET /evento
     */
    @GetMapping("/all")
    public ResponseEntity<List<EventoResponseDTO>> getAllEventos() {
        List<Evento> eventos = eventoService.getAllEvento();
        List<EventoResponseDTO> eventosDTO = eventos.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventosDTO);
    }

    /**
     * Obtiene un evento específico por su ID
     * GET /evento/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventoResponseDTO> getEventoById(@PathVariable Long id) {
        Optional<Evento> evento = eventoService.getEventoById(id);
        if (evento.isPresent()) {
            return ResponseEntity.ok(convertToResponseDTO(evento.get()));
        } else {
            throw new NotFoundException("Evento no encontrado con ID: " + id);
        }
    }

    /**
     * Crea un nuevo evento
     * POST /evento
     */
    @PostMapping("/crear")
    public ResponseEntity<EventoResponseDTO> createEvento(@RequestBody EventoDTO eventoDTO) {
        try {
            Evento evento = convertToEntity(eventoDTO);
            Evento eventoCreado = eventoService.createEvento(evento);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(convertToResponseDTO(eventoCreado));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crea un evento con ubicación específica
     * POST /evento/con-ubicacion
     */
    @PostMapping("/con-ubicacion")
    public ResponseEntity<EventoResponseDTO> createEventoConUbicacion(
            @RequestBody EventoConUbicacionDTO request) {
        try {
            Evento evento = convertToEntity(request.getEvento());
            Ubicacion ubicacion = convertUbicacionToEntity(request.getUbicacion());
            Evento eventoCreado = eventoService.createEventoConUbicacion(evento, ubicacion);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(convertToResponseDTO(eventoCreado));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualiza un evento existente
     * PUT /evento/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<EventoResponseDTO> updateEvento(
            @PathVariable Long id,
            @RequestBody EventoDTO eventoDTO) {
        try {
            Evento evento = convertToEntity(eventoDTO);
            Evento eventoActualizado = eventoService.updateEvento(id, evento);
            return ResponseEntity.ok(convertToResponseDTO(eventoActualizado));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina un evento
     * DELETE /evento/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvento(@PathVariable Long id) {
        try {
            Optional<Evento> evento = eventoService.getEventoById(id);
            if (evento.isPresent()) {
                eventoService.deleteEvento(evento.get());
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca eventos con filtros
     * GET /evento/buscar
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<EventoResponseDTO>> buscarEventos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String comuna,
            @RequestParam(required = false) Long productorId,
            @RequestParam(required = false) Boolean soloDisponibles) {
        
        List<Evento> eventos = eventoService.getAllEvento();
        
        // Aplicar filtros
        List<Evento> eventosFiltrados = eventos.stream()
                .filter(evento -> nombre == null || evento.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .filter(evento -> comuna == null || evento.getUbicacion().getComuna().toLowerCase().contains(comuna.toLowerCase()))
                .filter(evento -> productorId == null || evento.getProductor().getId().equals(productorId))
                .filter(evento -> soloDisponibles == null || !soloDisponibles || evento.isDisponible())
                .collect(Collectors.toList());
        
        List<EventoResponseDTO> eventosDTO = eventosFiltrados.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(eventosDTO);
    }

    /**
     * Obtiene eventos próximos (en los próximos 7 días)
     * GET /evento/proximos
     */
    @GetMapping("/proximos")
    public ResponseEntity<List<EventoResponseDTO>> getEventosProximos() {
        List<Evento> eventos = eventoService.getAllEvento();
        List<EventoResponseDTO> eventosProximos = eventos.stream()
                .filter(Evento::isEventoProximo)
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventosProximos);
    }

    /**
     * Obtiene eventos disponibles (con cupos y no pasados)
     * GET /evento/disponibles
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<EventoResponseDTO>> getEventosDisponibles() {
        List<Evento> eventos = eventoService.getAllEvento();
        List<EventoResponseDTO> eventosDisponibles = eventos.stream()
                .filter(Evento::isDisponible)
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventosDisponibles);
    }

    /**
     * Obtiene cupos disponibles de un evento
     * GET /evento/{id}/cupos
     */
    @GetMapping("/{id}/cupos")
    public ResponseEntity<Integer> getCuposDisponibles(@PathVariable Long id) {
        Optional<Evento> evento = eventoService.getEventoById(id);
        if (evento.isPresent()) {
            return ResponseEntity.ok(evento.get().getCuposDisponibles());
        } else {
            throw new NotFoundException("Evento no encontrado con ID: " + id);
        }
    }

    // Métodos de conversión privados

    /**
     * Convierte un Evento a EventoResponseDTO
     */
    private EventoResponseDTO convertToResponseDTO(Evento evento) {
        EventoResponseDTO dto = new EventoResponseDTO();
        dto.setId(evento.getId());
        dto.setNombre(evento.getNombre());
        dto.setDescripcion(evento.getDescripcion());
        dto.setFecha(evento.getFecha());
        dto.setCapacidadMaxima(evento.getCapacidadMaxima());
        dto.setPrecioEntrada(evento.getPrecioEntrada());
        dto.setImagenUrl(evento.getImagenUrl());
        dto.setActivo(evento.getActivo());
        dto.setFechaCreacion(evento.getFechaCreacion());
        dto.setFechaModificacion(evento.getFechaModificacion());
        
        // Campos calculados
        dto.setCuposDisponibles(evento.getCuposDisponibles());
        dto.setDisponible(evento.isDisponible());
        dto.setEventoPasado(evento.isEventoPasado());
        dto.setEventoProximo(evento.isEventoProximo());
        
        // TODO: Convertir ubicacion y productor a DTOs si es necesario
        // Por ahora se dejan como null para evitar dependencias circulares
        
        return dto;
    }

    /**
     * Convierte un EventoDTO a Evento
     */
    private Evento convertToEntity(EventoDTO dto) {
        Evento evento = new Evento();
        evento.setId(dto.getId());
        evento.setNombre(dto.getNombre());
        evento.setDescripcion(dto.getDescripcion());
        evento.setFecha(dto.getFecha());
        evento.setCapacidadMaxima(dto.getCapacidadMaxima());
        evento.setPrecioEntrada(dto.getPrecioEntrada());
        evento.setImagenUrl(dto.getImagenUrl());
        
        // Buscar y asignar productor por ID
        if (dto.getProductorId() != null) {
            Productor productor = productorRepository.findById(dto.getProductorId())
                .orElseThrow(() -> new NotFoundException("Productor no encontrado con ID: " + dto.getProductorId()));
            evento.setProductor(productor);
        }
        
        // Buscar y asignar ubicación por ID (solo si no se está creando una nueva)
        if (dto.getUbicacionId() != null) {
            Ubicacion ubicacion = ubicacionRepository.findById(dto.getUbicacionId())
                .orElseThrow(() -> new NotFoundException("Ubicación no encontrada con ID: " + dto.getUbicacionId()));
            evento.setUbicacion(ubicacion);
        }
        
        return evento;
    }

    /**
     * Convierte un UbicacionDTO a Ubicacion
     */
    private Ubicacion convertUbicacionToEntity(UbicacionDTO dto) {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setId(dto.getId());
        ubicacion.setDireccion(dto.getDireccion());
        ubicacion.setComuna(dto.getComuna());
        ubicacion.setLatitud(dto.getLatitud());
        ubicacion.setLongitud(dto.getLongitud());
        return ubicacion;
    }
}
