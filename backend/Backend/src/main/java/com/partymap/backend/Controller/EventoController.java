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

import com.partymap.backend.Config.SecurityUtils;
import com.partymap.backend.DTO.EventoConUbicacionDTO;
import com.partymap.backend.DTO.EventoDTO;
import com.partymap.backend.DTO.EventoResponseDTO;
import com.partymap.backend.DTO.ProductorResponseDTO;
import com.partymap.backend.DTO.UbicacionDTO;
import com.partymap.backend.DTO.UbicacionResponseDTO;
import com.partymap.backend.Exceptions.NotFoundException;
import com.partymap.backend.Model.Evento;
import com.partymap.backend.Model.Productor;
import com.partymap.backend.Model.Ubicacion;
import com.partymap.backend.Model.Usuario;
import com.partymap.backend.Repository.EventoRepository;
import com.partymap.backend.Repository.ProductorRepository;
import com.partymap.backend.Repository.UbicacionRepository;
import com.partymap.backend.Repository.UsuarioRepository;
import com.partymap.backend.Service.EventoService;

/**
 * Controlador REST para la gestión de eventos.
 * Proporciona endpoints para operaciones CRUD de eventos.
 */
@RestController
@CrossOrigin
@RequestMapping("/evento")
public class EventoController {

    private final EventoService eventoService;
    private final ProductorRepository productorRepository;
    private final UbicacionRepository ubicacionRepository;
    private final SecurityUtils securityUtils;

    public EventoController(EventoService eventoService, 
                          ProductorRepository productorRepository,
                          UbicacionRepository ubicacionRepository,
                          UsuarioRepository usuarioRepository,
                          EventoRepository eventoRepository,
                          SecurityUtils securityUtils) {
        this.eventoService = eventoService;
        this.productorRepository = productorRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.securityUtils = securityUtils;
    }

    /**
     * Obtiene todos los eventos
     * GET /evento/all
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
        var evento = eventoService.getEventoById(id);
        if (evento.isPresent()) {
            return ResponseEntity.ok(convertToResponseDTO(evento.get()));
        } else {
            throw new NotFoundException("Evento no encontrado con ID: " + id);
        }
    }

    /**
     * Crea un nuevo evento
     * POST /evento/crear
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
            // Verificar autenticación
            Optional<Usuario> currentUser = securityUtils.getCurrentUser();
            if (currentUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Usuario user = currentUser.get();
            
            // Solo productores y administradores pueden crear eventos
            if (!user.isProductor() && !user.isAdministrador()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Crear el evento sin productor (se asignará después)
            Evento evento = convertToEntityWithoutProductor(request.getEvento());
            
            // Asignar el productor
            if (user.isAdministrador()) {
                // Administradores pueden especificar cualquier productor o usar uno por defecto
                if (request.getEvento().getProductorId() != null) {
                    // Usar el productor especificado
                    Optional<Productor> productor = productorRepository.findById(request.getEvento().getProductorId());
                    if (productor.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(null); // Productor especificado no encontrado
                    }
                    evento.setProductor(productor.get());
                } else {
                    // Si no se especifica, buscar el primer productor disponible
                    List<Productor> productores = productorRepository.findAll();
                    if (productores.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(null); // No hay productores disponibles
                    }
                    evento.setProductor(productores.get(0));
                }
            } else if (user.isProductor()) {
                // Buscar el productor asociado al usuario
                Optional<Productor> productor = productorRepository.findByUsuarioId(user.getId());
                if (productor.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(null); // Productor no encontrado para el usuario
                }
                evento.setProductor(productor.get());
            }

            // Crear la ubicación
            Ubicacion ubicacion = convertUbicacionToEntity(request.getUbicacion());
            
            // Crear el evento con ubicación
            Evento eventoCreado = eventoService.createEventoConUbicacion(evento, ubicacion);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(convertToResponseDTO(eventoCreado));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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
            // Verificar autenticación
            Optional<Usuario> currentUser = securityUtils.getCurrentUser();
            if (currentUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Usuario user = currentUser.get();
            
            // Solo productores y administradores pueden actualizar eventos
            if (!user.isProductor() && !user.isAdministrador()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Verificar permisos (solo para productores)
            if (user.isProductor()) {
                Optional<Evento> existingEvento = eventoService.getEventoById(id);
                if (existingEvento.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                // Verificar que el productor sea el propietario del evento
                Optional<Productor> userProductor = productorRepository.findByUsuarioId(user.getId());
                if (userProductor.isEmpty() || !existingEvento.get().getProductor().getId().equals(userProductor.get().getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            Evento evento = convertToEntity(eventoDTO);
            Evento eventoActualizado = eventoService.updateEvento(id, evento);
            return ResponseEntity.ok(convertToResponseDTO(eventoActualizado));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Elimina un evento
     * DELETE /evento/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvento(@PathVariable Long id) {
        try {
            // Verificar autenticación
            Optional<Usuario> currentUser = securityUtils.getCurrentUser();
            if (currentUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Usuario user = currentUser.get();
            
            // Solo productores y administradores pueden eliminar eventos
            if (!user.isProductor() && !user.isAdministrador()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            var evento = eventoService.getEventoById(id);
            if (evento.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Verificar permisos (solo para productores)
            if (user.isProductor()) {
                Optional<Productor> userProductor = productorRepository.findByUsuarioId(user.getId());
                if (userProductor.isEmpty() || !evento.get().getProductor().getId().equals(userProductor.get().getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            eventoService.deleteEvento(evento.get());
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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
        var evento = eventoService.getEventoById(id);
        if (evento.isPresent()) {
            return ResponseEntity.ok(evento.get().getCuposDisponibles());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Métodos de conversión privados
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
        dto.setCuposDisponibles(evento.getCuposDisponibles());
        dto.setDisponible(evento.isDisponible());
        dto.setEventoPasado(evento.isEventoPasado());
        dto.setEventoProximo(evento.isEventoProximo());
        
        if (evento.getProductor() != null) {
            ProductorResponseDTO productorDTO = new ProductorResponseDTO();
            productorDTO.setId(evento.getProductor().getId());
            productorDTO.setNombreEmpresa(evento.getProductor().getNombreEmpresa());
            productorDTO.setRut(evento.getProductor().getRut());
            productorDTO.setActivo(evento.getProductor().getActivo());
            productorDTO.setFechaCreacion(evento.getProductor().getFechaCreacion());
            
            // Incluir información completa del usuario si existe
            if (evento.getProductor().getUsuario() != null) {
                com.partymap.backend.DTO.UsuarioResponseDTO usuarioDTO = new com.partymap.backend.DTO.UsuarioResponseDTO();
                usuarioDTO.setId(evento.getProductor().getUsuario().getId());
                usuarioDTO.setNombre(evento.getProductor().getUsuario().getNombre());
                usuarioDTO.setEmail(evento.getProductor().getUsuario().getEmail());
                usuarioDTO.setTipoUsuario(evento.getProductor().getUsuario().getTipoUsuario());
                usuarioDTO.setActivo(evento.getProductor().getUsuario().getActivo());
                usuarioDTO.setFechaCreacion(evento.getProductor().getUsuario().getFechaCreacion());
                productorDTO.setUsuario(usuarioDTO);
            }
            
            dto.setProductor(productorDTO);
        }
        
        if (evento.getUbicacion() != null) {
            UbicacionResponseDTO ubicacionDTO = new UbicacionResponseDTO();
            ubicacionDTO.setId(evento.getUbicacion().getId());
            ubicacionDTO.setDireccion(evento.getUbicacion().getDireccion());
            ubicacionDTO.setComuna(evento.getUbicacion().getComuna());
            ubicacionDTO.setLatitud(evento.getUbicacion().getLatitud());
            ubicacionDTO.setLongitud(evento.getUbicacion().getLongitud());
            ubicacionDTO.setActivo(evento.getUbicacion().getActivo());
            ubicacionDTO.setFechaCreacion(evento.getUbicacion().getFechaCreacion());
            dto.setUbicacion(ubicacionDTO);
        }
        
        return dto;
    }

    private Evento convertToEntity(EventoDTO dto) {
        Evento evento = new Evento();
        evento.setNombre(dto.getNombre());
        evento.setDescripcion(dto.getDescripcion());
        evento.setFecha(dto.getFecha());
        evento.setCapacidadMaxima(dto.getCapacidadMaxima());
        evento.setPrecioEntrada(dto.getPrecioEntrada());
        evento.setImagenUrl(dto.getImagenUrl());
        
        if (dto.getProductorId() != null) {
            var productor = productorRepository.findById(dto.getProductorId());
            productor.ifPresent(evento::setProductor);
        }
        
        if (dto.getUbicacionId() != null) {
            var ubicacion = ubicacionRepository.findById(dto.getUbicacionId());
            ubicacion.ifPresent(evento::setUbicacion);
        }
        
        return evento;
    }

    private Evento convertToEntityWithoutProductor(EventoDTO dto) {
        Evento evento = new Evento();
        evento.setNombre(dto.getNombre());
        evento.setDescripcion(dto.getDescripcion());
        evento.setFecha(dto.getFecha());
        evento.setCapacidadMaxima(dto.getCapacidadMaxima());
        evento.setPrecioEntrada(dto.getPrecioEntrada());
        evento.setImagenUrl(dto.getImagenUrl());
        
        return evento;
    }

    private Ubicacion convertUbicacionToEntity(UbicacionDTO dto) {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setDireccion(dto.getDireccion());
        ubicacion.setComuna(dto.getComuna());
        ubicacion.setLatitud(dto.getLatitud());
        ubicacion.setLongitud(dto.getLongitud());
        return ubicacion;
    }
}
