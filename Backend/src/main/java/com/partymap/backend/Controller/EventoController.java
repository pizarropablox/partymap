package com.partymap.backend.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.partymap.backend.DTO.EventoConUbicacionDTO;
import com.partymap.backend.DTO.EventoConUbicacionUpdateDTO;
import com.partymap.backend.DTO.EventoDTO;
import com.partymap.backend.DTO.EventoResponseDTO;
import com.partymap.backend.DTO.UbicacionDTO;
import com.partymap.backend.DTO.UbicacionResponseDTO;
import com.partymap.backend.Exceptions.NotFoundException;
import com.partymap.backend.Model.Evento;
import com.partymap.backend.Model.Ubicacion;
import com.partymap.backend.Model.Usuario;
import com.partymap.backend.Repository.EventoRepository;
import com.partymap.backend.Repository.UbicacionRepository;
import com.partymap.backend.Repository.UsuarioRepository;
import com.partymap.backend.Service.EventoService;
import com.partymap.backend.Config.SecurityUtils;

/**
 * Controlador REST para la gestión de eventos.
 * Proporciona endpoints para crear, leer, actualizar y eliminar eventos.
 * Incluye funcionalidades de búsqueda y filtrado.
 */
@RestController
@RequestMapping("/evento")
public class EventoController {

    private final EventoService eventoService;
    private final UbicacionRepository ubicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final SecurityUtils securityUtils;

    public EventoController(EventoService eventoService, 
                          UbicacionRepository ubicacionRepository,
                          UsuarioRepository usuarioRepository,
                          EventoRepository eventoRepository,
                          SecurityUtils securityUtils) {
        this.eventoService = eventoService;
        this.ubicacionRepository = ubicacionRepository;
        this.usuarioRepository = usuarioRepository;
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
     * Crea un nuevo evento con ubicación
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

            Usuario organizador = currentUser.get();

            // Verificar permisos
            if (!organizador.isProductor() && !organizador.isAdministrador()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }

            // Validar datos del evento antes de procesar
            if (request.getEvento() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Validar nombre del evento
            String nombre = request.getEvento().getNombre();
            if (nombre == null || nombre.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            if (nombre.trim().length() < 3) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            if (nombre.trim().length() > 100) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Validar fecha del evento
            if (request.getEvento().getFecha() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Validar que la fecha no sea en el pasado
            if (request.getEvento().getFecha().isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Validar descripción del evento (mínimo 10 caracteres)
            String descripcion = request.getEvento().getDescripcion();
            if (descripcion == null || descripcion.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            if (descripcion.trim().length() < 10) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            if (descripcion.trim().length() > 2000) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Validar datos de la ubicación
            if (request.getUbicacion() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Validar dirección
            String direccion = request.getUbicacion().getDireccion();
            if (direccion == null || direccion.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            if (direccion.trim().length() < 5) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Validar comuna
            String comuna = request.getUbicacion().getComuna();
            if (comuna == null || comuna.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            if (comuna.trim().length() < 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Validar coordenadas
            if (request.getUbicacion().getLatitud() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            if (request.getUbicacion().getLongitud() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Crear el evento con el usuario organizador asignado
            Evento evento = convertToEntityWithoutUsuario(request.getEvento());
            evento.setUsuario(organizador);

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
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede actualizar cualquier evento
     * - PRODUCTOR: Solo puede actualizar sus propios eventos
     * - CLIENTE: No puede actualizar eventos
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
                
                // Verificar que el usuario sea el propietario del evento
                if (!existingEvento.get().getUsuario().getId().equals(user.getId())) {
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
     * Actualiza un evento existente con ubicación
     * PUT /evento/{id}/con-ubicacion
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede actualizar cualquier evento
     * - PRODUCTOR: Solo puede actualizar sus propios eventos
     * - CLIENTE: No puede actualizar eventos
     */
    @PutMapping("/{id}/con-ubicacion")
    public ResponseEntity<EventoResponseDTO> updateEventoConUbicacion(
            @PathVariable Long id,
            @RequestBody EventoConUbicacionUpdateDTO request) {
        
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
                
                // Verificar que el usuario sea el propietario del evento
                if (!existingEvento.get().getUsuario().getId().equals(user.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            // Validar datos del evento antes de procesar
            if (request.getEvento() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Validar nombre del evento
            String nombre = request.getEvento().getNombre();
            if (nombre == null || nombre.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            if (nombre.trim().length() < 3) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            if (nombre.trim().length() > 100) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Validar fecha del evento
            if (request.getEvento().getFecha() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Validar que la fecha no sea en el pasado
            if (request.getEvento().getFecha().isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Validar descripción del evento (mínimo 10 caracteres)
            String descripcion = request.getEvento().getDescripcion();
            if (descripcion == null || descripcion.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            if (descripcion.trim().length() < 10) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            if (descripcion.trim().length() > 2000) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Validar datos de la ubicación
            if (request.getUbicacion() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Validar dirección
            String direccion = request.getUbicacion().getDireccion();
            if (direccion == null || direccion.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            if (direccion.trim().length() < 5) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Validar comuna
            String comuna = request.getUbicacion().getComuna();
            if (comuna == null || comuna.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            if (comuna.trim().length() < 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            // Convertir DTOs a entidades
            Evento evento = convertToEntity(request.getEvento());
            Ubicacion ubicacion = convertUbicacionToEntity(request.getUbicacion());

            // Actualizar evento con ubicación
            Evento eventoActualizado = eventoService.updateEventoConUbicacion(id, evento, ubicacion);
            return ResponseEntity.ok(convertToResponseDTO(eventoActualizado));
            
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Elimina un evento (soft delete)
     * DELETE /evento/{id}
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede eliminar cualquier evento
     * - PRODUCTOR: Solo puede eliminar sus propios eventos
     * - CLIENTE: No puede eliminar eventos
     */
    @DeleteMapping("/eliminar/{id}")
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

            // Buscar el evento a eliminar
            Optional<Evento> evento = eventoService.getEventoById(id);
            if (evento.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Verificar permisos (solo para productores)
            if (user.isProductor()) {
                if (!evento.get().getUsuario().getId().equals(user.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            eventoService.deleteEvento(evento.get());
            return ResponseEntity.noContent().build();
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
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Boolean soloDisponibles) {
        
        List<Evento> eventos = eventoService.getAllEvento();
        
        // Aplicar filtros
        if (nombre != null) {
            eventos = eventos.stream()
                    .filter(evento -> evento.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (comuna != null) {
            eventos = eventos.stream()
                    .filter(evento -> evento.getUbicacion() != null && 
                            evento.getUbicacion().getComuna().toLowerCase().contains(comuna.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (usuarioId != null) {
            eventos = eventos.stream()
                    .filter(evento -> evento.getUsuario() != null && evento.getUsuario().getId().equals(usuarioId))
                    .collect(Collectors.toList());
        }
        
        if (soloDisponibles != null && soloDisponibles) {
            eventos = eventos.stream()
                    .filter(Evento::isDisponible)
                    .collect(Collectors.toList());
        }
        
        List<EventoResponseDTO> eventosDTO = eventos.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(eventosDTO);
    }

    /**
     * Obtiene eventos próximos (en las próximas 24 horas)
     * GET /evento/proximos
     */
    @GetMapping("/proximos")
    public ResponseEntity<List<EventoResponseDTO>> getEventosProximos() {
        List<Evento> eventos = eventoService.getAllEvento();
        List<EventoResponseDTO> eventosDTO = eventos.stream()
                .filter(Evento::isEventoProximo)
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(eventosDTO);
    }

    /**
     * Obtiene eventos disponibles para reservas
     * GET /evento/disponibles
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<EventoResponseDTO>> getEventosDisponibles() {
        List<Evento> eventos = eventoService.getAllEvento();
        List<EventoResponseDTO> eventosDTO = eventos.stream()
                .filter(Evento::isDisponible)
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(eventosDTO);
    }

    /**
     * Obtiene cupos disponibles para un evento
     * GET /evento/{id}/cupos
     */
    @GetMapping("/{id}/cupos")
    public ResponseEntity<Integer> getCuposDisponibles(@PathVariable Long id) {
        Optional<Evento> evento = eventoService.getEventoById(id);
        if (evento.isEmpty()) {
            throw new NotFoundException("Evento no encontrado con ID: " + id);
        }
        
        return ResponseEntity.ok(evento.get().getCuposDisponibles());
    }

    /**
     * Obtiene eventos de un usuario productor específico
     * GET /evento/usuario/{usuarioId}
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede ver eventos de cualquier productor
     * - PRODUCTOR: Solo puede ver sus propios eventos
     * - CLIENTE: Puede ver eventos de cualquier productor
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<EventoResponseDTO>> getEventosPorUsuario(@PathVariable Long usuarioId) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Verificar que el usuario existe y es productor
        Optional<Usuario> usuarioProductor = usuarioRepository.findById(usuarioId);
        if (usuarioProductor.isEmpty() || !usuarioProductor.get().isProductor()) {
            throw new NotFoundException("Usuario productor no encontrado con ID: " + usuarioId);
        }

        // Verificar permisos
        if (user.isProductor() && !user.getId().equals(usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Evento> eventos = eventoService.getAllEvento();
        List<Evento> eventosUsuario = eventos.stream()
                .filter(evento -> evento.getUsuario() != null &&
                        evento.getUsuario().getId().equals(usuarioId))
                .collect(Collectors.toList());
        
        List<EventoResponseDTO> eventosDTO = eventosUsuario.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(eventosDTO);
    }

    /**
     * Obtiene eventos de un usuario productor específico
     * GET /evento/productor/{id}
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede ver eventos de cualquier productor
     * - PRODUCTOR: Solo puede ver sus propios eventos
     * - CLIENTE: Puede ver eventos de cualquier productor
     */
    @GetMapping("/productor/{id}")
    public ResponseEntity<List<EventoResponseDTO>> getEventosPorProductor(@PathVariable Long id) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Verificar que el usuario existe y es productor
        Optional<Usuario> usuarioProductor = usuarioRepository.findById(id);
        if (usuarioProductor.isEmpty() || !usuarioProductor.get().isProductor()) {
            throw new NotFoundException("Usuario productor no encontrado con ID: " + id);
        }

        // Verificar permisos de acceso
        if (user.isProductor() && !user.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Evento> eventos = eventoService.getEventosByUsuarioId(id);
        List<EventoResponseDTO> eventosDTO = eventos.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventosDTO);
    }

    /**
     * Obtiene eventos del usuario autenticado
     * GET /evento/mis-eventos
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede ver todos los eventos
     * - PRODUCTOR: Solo puede ver sus propios eventos
     * - CLIENTE: No puede ver eventos
     */
    @GetMapping("/mis-eventos")
    public ResponseEntity<List<EventoResponseDTO>> getMisEventos() {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores y productores pueden ver eventos
        if (!user.isAdministrador() && !user.isProductor()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Evento> eventos = eventoService.getAllEvento();
        
        if (user.isAdministrador()) {
            // Administradores ven todos los eventos
            List<EventoResponseDTO> eventosDTO = eventos.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(eventosDTO);
        } else if (user.isProductor()) {
            // Productores solo ven sus propios eventos
            List<Evento> misEventos = eventos.stream()
                    .filter(evento -> evento.getUsuario() != null &&
                            evento.getUsuario().getId().equals(user.getId()))
                    .collect(Collectors.toList());
            
            List<EventoResponseDTO> eventosDTO = misEventos.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(eventosDTO);
        }
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Obtiene estadísticas de eventos del usuario autenticado
     * GET /evento/mis-estadisticas
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede ver estadísticas de todos los eventos
     * - PRODUCTOR: Solo puede ver estadísticas de sus propios eventos
     * - CLIENTE: No puede ver estadísticas
     */
    @GetMapping("/mis-estadisticas")
    public ResponseEntity<Object> getMisEstadisticas() {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores y productores pueden ver estadísticas
        if (!user.isAdministrador() && !user.isProductor()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Evento> eventos = eventoService.getAllEvento();
        
        if (user.isProductor()) {
            // Filtrar solo eventos del productor
            eventos = eventos.stream()
                    .filter(evento -> evento.getUsuario() != null &&
                            evento.getUsuario().getId().equals(user.getId()))
                    .collect(Collectors.toList());
        }

        long totalEventos = eventos.size();
        long eventosActivos = eventos.stream().filter(e -> e.getActivo() == 1).count();
        long eventosInactivos = eventos.stream().filter(e -> e.getActivo() == 0).count();
        long eventosDisponibles = eventos.stream().filter(Evento::isDisponible).count();
        long eventosProximos = eventos.stream().filter(Evento::isEventoProximo).count();
        long eventosPasados = eventos.stream().filter(Evento::isEventoPasado).count();
        
        return ResponseEntity.ok(java.util.Map.of(
            "totalEventos", totalEventos,
            "eventosActivos", eventosActivos,
            "eventosInactivos", eventosInactivos,
            "eventosDisponibles", eventosDisponibles,
            "eventosProximos", eventosProximos,
            "eventosPasados", eventosPasados
        ));
    }

    /**
     * Busca eventos del usuario autenticado con filtros
     * GET /evento/mis-eventos/buscar
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede buscar en todos los eventos
     * - PRODUCTOR: Solo puede buscar en sus propios eventos
     * - CLIENTE: No puede buscar eventos
     */
    @GetMapping("/mis-eventos/buscar")
    public ResponseEntity<List<EventoResponseDTO>> buscarMisEventos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String comuna,
            @RequestParam(required = false) Boolean soloActivos,
            @RequestParam(required = false) Boolean soloDisponibles,
            @RequestParam(required = false) Boolean soloProximos,
            @RequestParam(required = false) Boolean soloPasados) {
        
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores y productores pueden buscar eventos
        if (!user.isAdministrador() && !user.isProductor()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Evento> eventos = eventoService.getAllEvento();
        
        // Filtrar por usuario si es productor
        if (user.isProductor()) {
            eventos = eventos.stream()
                    .filter(evento -> evento.getUsuario() != null &&
                            evento.getUsuario().getId().equals(user.getId()))
                    .collect(Collectors.toList());
        }
        
        // Aplicar filtros adicionales
        if (nombre != null) {
            eventos = eventos.stream()
                    .filter(evento -> evento.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (comuna != null) {
            eventos = eventos.stream()
                    .filter(evento -> evento.getUbicacion() != null && 
                            evento.getUbicacion().getComuna().toLowerCase().contains(comuna.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (soloActivos != null && soloActivos) {
            eventos = eventos.stream()
                    .filter(evento -> evento.getActivo() == 1)
                    .collect(Collectors.toList());
        }
        
        if (soloDisponibles != null && soloDisponibles) {
            eventos = eventos.stream()
                    .filter(Evento::isDisponible)
                    .collect(Collectors.toList());
        }
        
        if (soloProximos != null && soloProximos) {
            eventos = eventos.stream()
                    .filter(Evento::isEventoProximo)
                    .collect(Collectors.toList());
        }
        
        if (soloPasados != null && soloPasados) {
            eventos = eventos.stream()
                    .filter(Evento::isEventoPasado)
                    .collect(Collectors.toList());
        }
        
        List<EventoResponseDTO> eventosDTO = eventos.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(eventosDTO);
    }

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
        
        // Convertir ubicación
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
        
        // Convertir usuario productor
        if (evento.getUsuario() != null) {
            dto.setUsuarioId(evento.getUsuario().getId());
            dto.setUsuarioNombre(evento.getUsuario().getNombre());
            dto.setUsuarioEmail(evento.getUsuario().getEmail());
            dto.setUsuarioRutProductor(evento.getUsuario().getRutProductor());
        }
        
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
        
        // Asignar usuario si se especifica
        if (dto.getUsuarioId() != null) {
            Optional<Usuario> usuario = usuarioRepository.findById(dto.getUsuarioId());
            usuario.ifPresent(evento::setUsuario);
        }
        
        return evento;
    }

    /**
     * Convierte un EventoDTO a Evento sin asignar usuario
     */
    private Evento convertToEntityWithoutUsuario(EventoDTO dto) {
        Evento evento = new Evento();
        evento.setId(dto.getId());
        evento.setNombre(dto.getNombre());
        evento.setDescripcion(dto.getDescripcion());
        evento.setFecha(dto.getFecha());
        evento.setCapacidadMaxima(dto.getCapacidadMaxima());
        evento.setPrecioEntrada(dto.getPrecioEntrada());
        evento.setImagenUrl(dto.getImagenUrl());
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
