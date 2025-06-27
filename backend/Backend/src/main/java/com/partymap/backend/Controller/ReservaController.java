package com.partymap.backend.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;

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
import com.partymap.backend.DTO.ReservaDTO;
import com.partymap.backend.DTO.ReservaResponseDTO;
import com.partymap.backend.Exceptions.NotFoundException;
import com.partymap.backend.Model.Evento;
import com.partymap.backend.Model.Reserva;
import com.partymap.backend.Model.Usuario;
import com.partymap.backend.Repository.EventoRepository;
import com.partymap.backend.Repository.UsuarioRepository;
import com.partymap.backend.Service.ReservaService;

/**
 * Controlador REST para la gestión de reservas.
 * Proporciona endpoints para operaciones CRUD de reservas y funcionalidades adicionales
 * que aprovechan todas las capacidades del servicio mejorado.
 * 
 * SEGURIDAD:
 * - CLIENTE: Puede crear, ver y modificar sus propias reservas
 * - PRODUCTOR: Puede ver todas las reservas de sus eventos
 * - ADMINISTRADOR: Acceso completo a todas las reservas
 */
@RestController
@CrossOrigin
@RequestMapping("/reserva")
public class ReservaController {

    private final ReservaService reservaService;
    private final SecurityUtils securityUtils;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReservaController(ReservaService reservaService, SecurityUtils securityUtils, EventoRepository eventoRepository, UsuarioRepository usuarioRepository) {
        this.reservaService = reservaService;
        this.securityUtils = securityUtils;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Endpoint de prueba para verificar el rol del usuario autenticado
     * GET /reserva/test-auth
     */
    @GetMapping("/test-auth")
    public ResponseEntity<Map<String, Object>> testAuth() {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        Optional<String> currentUserEmail = securityUtils.getCurrentUserEmail();
        
        Map<String, Object> response = new HashMap<>();
        
        if (currentUser.isEmpty()) {
            response.put("authenticated", false);
            response.put("message", "Usuario no encontrado en la base de datos");
            response.put("emailFromJWT", currentUserEmail.orElse("No encontrado"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Usuario user = currentUser.get();
        response.put("authenticated", true);
        response.put("message", "Usuario autenticado correctamente");
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("emailFromJWT", currentUserEmail.orElse("No encontrado"));
        response.put("tipoUsuario", user.getTipoUsuario());
        response.put("isCliente", user.isCliente());
        response.put("isProductor", user.isProductor());
        response.put("isAdministrador", user.isAdministrador());
        response.put("azureB2cId", user.getAzureB2cId());
        response.put("esUsuarioAzure", user.getEsUsuarioAzure());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene todas las reservas del sistema
     * GET /reserva
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Ve todas las reservas
     * - PRODUCTOR: Ve todas las reservas
     * - CLIENTE: Ve solo sus propias reservas
     */
    @GetMapping("/all")
    public ResponseEntity<List<ReservaResponseDTO>> getAllReservas() {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Reserva> reservas;
        Usuario user = currentUser.get();

        if (user.isAdministrador() || user.isProductor()) {
            // Admin y productores ven todas las reservas
            reservas = reservaService.getAllreservas();
        } else if (user.isCliente()) {
            // Clientes solo ven sus propias reservas
            reservas = reservaService.getReservasByUsuarioId(user.getId());
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Obtiene una reserva por su ID
     * GET /reserva/{id}
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede ver cualquier reserva
     * - PRODUCTOR: Puede ver cualquier reserva
     * - CLIENTE: Solo puede ver sus propias reservas
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> getReservaById(@PathVariable Long id) {
        Optional<Reserva> reserva = reservaService.getReservaById(id);
        if (reserva.isEmpty()) {
            throw new NotFoundException("Reserva no encontrada con ID: " + id);
        }

        // Verificar permisos de acceso
        if (!securityUtils.canAccessReserva(reserva.get().getUsuario().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(convertToResponseDTO(reserva.get()));
    }

    /**
     * Crea una nueva reserva
     * POST /reserva
     * 
     * SEGURIDAD:
     * - CLIENTE: Puede crear reservas
     * - PRODUCTOR: No puede crear reservas (debe ser cliente)
     * - ADMINISTRADOR: Puede crear reservas
     */
    @PostMapping("/crear")
    public ResponseEntity<ReservaResponseDTO> createReserva(@RequestBody ReservaDTO reservaDTO) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo clientes y administradores pueden crear reservas
        if (!user.isCliente() && !user.isAdministrador()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Reserva reserva = convertToEntity(reservaDTO);
            
            // Si es cliente, asegurar que la reserva se asigne a su usuario
            if (user.isCliente()) {
                reserva.setUsuario(user);
            }
            
            Reserva reservaCreada = reservaService.createReserva(reserva);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(convertToResponseDTO(reservaCreada));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualiza una reserva existente
     * PUT /reserva/{id}
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede modificar cualquier reserva
     * - CLIENTE: Solo puede modificar sus propias reservas
     * - PRODUCTOR: No puede modificar reservas
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> updateReserva(
            @PathVariable Long id,
            @RequestBody ReservaDTO reservaDTO) {
        Optional<Reserva> existingReserva = reservaService.getReservaById(id);
        if (existingReserva.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verificar permisos de modificación
        if (!securityUtils.canModifyReserva(existingReserva.get().getUsuario().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Reserva reserva = convertToEntity(reservaDTO);
            Reserva reservaActualizada = reservaService.updateReserva(id, reserva);
            return ResponseEntity.ok(convertToResponseDTO(reservaActualizada));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Elimina una reserva
     * DELETE /reserva/{id}
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR puede eliminar reservas
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReserva(@PathVariable Long id) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Solo administradores pueden eliminar reservas
        if (!currentUser.get().isAdministrador()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<Reserva> reserva = reservaService.getReservaById(id);
            if (reserva.isPresent()) {
                reservaService.deleteReserva(reserva.get());
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todas las reservas de un usuario específico
     * GET /reserva/usuario/{usuarioId}
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede ver reservas de cualquier usuario
     * - PRODUCTOR: Puede ver reservas de cualquier usuario
     * - CLIENTE: Solo puede ver sus propias reservas
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasByUsuario(@PathVariable Long usuarioId) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Clientes solo pueden ver sus propias reservas
        if (user.isCliente() && !user.getId().equals(usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Reserva> reservas = reservaService.getReservasByUsuarioId(usuarioId);
        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Obtiene todas las reservas de un evento específico
     * GET /reserva/evento/{eventoId}
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede ver reservas de cualquier evento
     * - PRODUCTOR: Puede ver reservas de cualquier evento
     * - CLIENTE: No puede ver reservas de eventos (solo las suyas)
     */
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasByEvento(@PathVariable Long eventoId) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores y productores pueden ver reservas de eventos
        if (user.isCliente()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Reserva> reservas = reservaService.getReservasByEventoId(eventoId);
        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Obtiene todas las reservas activas
     * GET /reserva/activas
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede ver todas las reservas activas
     * - PRODUCTOR: Puede ver todas las reservas activas
     * - CLIENTE: Solo puede ver sus propias reservas activas
     */
    @GetMapping("/activas")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasActivas() {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Reserva> reservas;
        Usuario user = currentUser.get();

        if (user.isAdministrador() || user.isProductor()) {
            // Admin y productores ven todas las reservas activas
            reservas = reservaService.getReservasActivas();
        } else if (user.isCliente()) {
            // Clientes solo ven sus propias reservas activas
            reservas = reservaService.getReservasByUsuarioId(user.getId()).stream()
                    .filter(Reserva::isActiva)
                    .collect(Collectors.toList());
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Obtiene todas las reservas canceladas
     * GET /reserva/canceladas
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede ver todas las reservas canceladas
     * - PRODUCTOR: Puede ver todas las reservas canceladas
     * - CLIENTE: Solo puede ver sus propias reservas canceladas
     */
    @GetMapping("/canceladas")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasCanceladas() {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Reserva> reservas;
        Usuario user = currentUser.get();

        if (user.isAdministrador() || user.isProductor()) {
            // Admin y productores ven todas las reservas canceladas
            reservas = reservaService.getReservasCanceladas();
        } else if (user.isCliente()) {
            // Clientes solo ven sus propias reservas canceladas
            reservas = reservaService.getReservasByUsuarioId(user.getId()).stream()
                    .filter(Reserva::isCancelada)
                    .collect(Collectors.toList());
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Cancela una reserva
     * PUT /reserva/{id}/cancelar
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede cancelar cualquier reserva
     * - CLIENTE: Solo puede cancelar sus propias reservas
     * - PRODUCTOR: No puede cancelar reservas
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponseDTO> cancelarReserva(@PathVariable Long id) {
        Optional<Reserva> reserva = reservaService.getReservaById(id);
        if (reserva.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verificar permisos de modificación
        if (!securityUtils.canModifyReserva(reserva.get().getUsuario().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Reserva reservaCancelada = reservaService.cancelarReserva(id);
            return ResponseEntity.ok(convertToResponseDTO(reservaCancelada));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Reactiva una reserva cancelada
     * PUT /reserva/{id}/reactivar
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR puede reactivar reservas
     */
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<ReservaResponseDTO> reactivarReserva(@PathVariable Long id) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Solo administradores pueden reactivar reservas
        if (!currentUser.get().isAdministrador()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Reserva reservaReactivada = reservaService.reactivarReserva(id);
            return ResponseEntity.ok(convertToResponseDTO(reservaReactivada));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene el precio total de una reserva específica
     * GET /reserva/{id}/precio-total
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede ver precio de cualquier reserva
     * - PRODUCTOR: Puede ver precio de cualquier reserva
     * - CLIENTE: Solo puede ver precio de sus propias reservas
     */
    @GetMapping("/{id}/precio-total")
    public ResponseEntity<BigDecimal> getPrecioTotal(@PathVariable Long id) {
        Optional<Reserva> reserva = reservaService.getReservaById(id);
        if (reserva.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verificar permisos de acceso
        if (!securityUtils.canAccessReserva(reserva.get().getUsuario().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            BigDecimal precioTotal = reservaService.getPrecioTotalReserva(id);
            return ResponseEntity.ok(precioTotal);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene reservas por rango de fechas
     * GET /reserva/rango-fechas
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR y PRODUCTOR pueden usar este endpoint
     */
    @GetMapping("/rango-fechas")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasPorRangoFechas(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores y productores pueden usar este endpoint
        if (user.isCliente()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio, DATE_FORMATTER);
            LocalDateTime fin = LocalDateTime.parse(fechaFin, DATE_FORMATTER);
            
            List<Reserva> reservas = reservaService.getReservasPorRangoFechas(inicio, fin);
            List<ReservaResponseDTO> reservasDTO = reservas.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reservasDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene reservas de un usuario en un rango de fechas
     * GET /reserva/usuario/{usuarioId}/rango-fechas
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede ver reservas de cualquier usuario
     * - PRODUCTOR: Puede ver reservas de cualquier usuario
     * - CLIENTE: Solo puede ver sus propias reservas
     */
    @GetMapping("/usuario/{usuarioId}/rango-fechas")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasUsuarioPorRangoFechas(
            @PathVariable Long usuarioId,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Clientes solo pueden ver sus propias reservas
        if (user.isCliente() && !user.getId().equals(usuarioId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio, DATE_FORMATTER);
            LocalDateTime fin = LocalDateTime.parse(fechaFin, DATE_FORMATTER);
            
            List<Reserva> reservas = reservaService.getReservasUsuarioPorRangoFechas(usuarioId, inicio, fin);
            List<ReservaResponseDTO> reservasDTO = reservas.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reservasDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene reservas de un evento en un rango de fechas
     * GET /reserva/evento/{eventoId}/rango-fechas
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR y PRODUCTOR pueden usar este endpoint
     */
    @GetMapping("/evento/{eventoId}/rango-fechas")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasEventoPorRangoFechas(
            @PathVariable Long eventoId,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores y productores pueden usar este endpoint
        if (user.isCliente()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            LocalDateTime inicio = LocalDateTime.parse(fechaInicio, DATE_FORMATTER);
            LocalDateTime fin = LocalDateTime.parse(fechaFin, DATE_FORMATTER);
            
            List<Reserva> reservas = reservaService.getReservasEventoPorRangoFechas(eventoId, inicio, fin);
            List<ReservaResponseDTO> reservasDTO = reservas.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reservasDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene reservas con precio total mayor a un valor específico
     * GET /reserva/precio-minimo
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR y PRODUCTOR pueden usar este endpoint
     */
    @GetMapping("/precio-minimo")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasPorPrecioMinimo(
            @RequestParam BigDecimal precioMinimo) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores y productores pueden usar este endpoint
        if (user.isCliente()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Reserva> reservas = reservaService.getReservasPorPrecioMinimo(precioMinimo);
        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Obtiene reservas con precio total menor a un valor específico
     * GET /reserva/precio-maximo
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR y PRODUCTOR pueden usar este endpoint
     */
    @GetMapping("/precio-maximo")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasPorPrecioMaximo(
            @RequestParam BigDecimal precioMaximo) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores y productores pueden usar este endpoint
        if (user.isCliente()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Reserva> reservas = reservaService.getReservasPorPrecioMaximo(precioMaximo);
        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Obtiene reservas por cantidad de entradas
     * GET /reserva/cantidad/{cantidad}
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR y PRODUCTOR pueden usar este endpoint
     */
    @GetMapping("/cantidad/{cantidad}")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasPorCantidad(@PathVariable Integer cantidad) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores y productores pueden usar este endpoint
        if (user.isCliente()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Reserva> reservas = reservaService.getReservasPorCantidad(cantidad);
        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Obtiene reservas con cantidad mayor a un valor específico
     * GET /reserva/cantidad-minima
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR y PRODUCTOR pueden usar este endpoint
     */
    @GetMapping("/cantidad-minima")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasPorCantidadMinima(
            @RequestParam Integer cantidadMinima) {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores y productores pueden usar este endpoint
        if (user.isCliente()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Reserva> reservas = reservaService.getReservasPorCantidadMinima(cantidadMinima);
        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Verifica si una reserva está activa
     * GET /reserva/{id}/activa
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede ver estado de cualquier reserva
     * - PRODUCTOR: Puede ver estado de cualquier reserva
     * - CLIENTE: Solo puede ver estado de sus propias reservas
     */
    @GetMapping("/{id}/activa")
    public ResponseEntity<Boolean> isReservaActiva(@PathVariable Long id) {
        Optional<Reserva> reserva = reservaService.getReservaById(id);
        if (reserva.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verificar permisos de acceso
        if (!securityUtils.canAccessReserva(reserva.get().getUsuario().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean isActiva = reservaService.isReservaActiva(id);
        return ResponseEntity.ok(isActiva);
    }

    /**
     * Verifica si una reserva está cancelada
     * GET /reserva/{id}/cancelada
     * 
     * SEGURIDAD:
     * - ADMINISTRADOR: Puede ver estado de cualquier reserva
     * - PRODUCTOR: Puede ver estado de cualquier reserva
     * - CLIENTE: Solo puede ver estado de sus propias reservas
     */
    @GetMapping("/{id}/cancelada")
    public ResponseEntity<Boolean> isReservaCancelada(@PathVariable Long id) {
        Optional<Reserva> reserva = reservaService.getReservaById(id);
        if (reserva.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verificar permisos de acceso
        if (!securityUtils.canAccessReserva(reserva.get().getUsuario().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean isCancelada = reservaService.isReservaCancelada(id);
        return ResponseEntity.ok(isCancelada);
    }

    /**
     * Busca reservas con filtros avanzados
     * GET /reserva/buscar
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR y PRODUCTOR pueden usar este endpoint
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ReservaResponseDTO>> buscarReservas(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long eventoId,
            @RequestParam(required = false) Boolean soloActivas,
            @RequestParam(required = false) Boolean soloCanceladas,
            @RequestParam(required = false) BigDecimal precioMinimo,
            @RequestParam(required = false) BigDecimal precioMaximo,
            @RequestParam(required = false) Integer cantidadMinima) {
        
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores y productores pueden usar este endpoint
        if (user.isCliente()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Reserva> reservas = reservaService.getAllreservas();
        
        // Aplicar filtros
        List<Reserva> reservasFiltradas = reservas.stream()
                .filter(reserva -> usuarioId == null || reserva.getUsuario().getId().equals(usuarioId))
                .filter(reserva -> eventoId == null || reserva.getEvento().getId().equals(eventoId))
                .filter(reserva -> soloActivas == null || !soloActivas || reserva.isActiva())
                .filter(reserva -> soloCanceladas == null || !soloCanceladas || reserva.isCancelada())
                .filter(reserva -> precioMinimo == null || reserva.getPrecioTotal().compareTo(precioMinimo) >= 0)
                .filter(reserva -> precioMaximo == null || reserva.getPrecioTotal().compareTo(precioMaximo) <= 0)
                .filter(reserva -> cantidadMinima == null || reserva.getCantidad() >= cantidadMinima)
                .collect(Collectors.toList());
        
        List<ReservaResponseDTO> reservasDTO = reservasFiltradas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Obtiene estadísticas completas de reservas
     * GET /reserva/estadisticas
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR y PRODUCTOR pueden usar este endpoint
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Object> getEstadisticas() {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores y productores pueden usar este endpoint
        if (user.isCliente()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Object estadisticas = reservaService.getEstadisticasCompletas();
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtiene estadísticas básicas de reservas (método anterior mantenido por compatibilidad)
     * GET /reserva/estadisticas-basicas
     * 
     * SEGURIDAD:
     * - Solo ADMINISTRADOR y PRODUCTOR pueden usar este endpoint
     */
    @GetMapping("/estadisticas-basicas")
    public ResponseEntity<Object> getEstadisticasBasicas() {
        Optional<Usuario> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Usuario user = currentUser.get();
        
        // Solo administradores y productores pueden usar este endpoint
        if (user.isCliente()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Reserva> todasLasReservas = reservaService.getAllreservas();
        List<Reserva> reservasActivas = reservaService.getReservasActivas();
        
        long totalReservas = todasLasReservas.size();
        long reservasActivasCount = reservasActivas.size();
        long reservasCanceladas = todasLasReservas.stream()
                .filter(Reserva::isCancelada)
                .count();
        
        double totalIngresos = todasLasReservas.stream()
                .filter(Reserva::isActiva)
                .mapToDouble(reserva -> reserva.getPrecioTotal().doubleValue())
                .sum();
        
        return ResponseEntity.ok(Map.of(
            "totalReservas", totalReservas,
            "reservasActivas", reservasActivasCount,
            "reservasCanceladas", reservasCanceladas,
            "totalIngresos", totalIngresos
        ));
    }

    // Métodos de conversión privados

    /**
     * Convierte un Reserva a ReservaResponseDTO
     */
    private ReservaResponseDTO convertToResponseDTO(Reserva reserva) {
        ReservaResponseDTO dto = new ReservaResponseDTO();
        dto.setId(reserva.getId());
        dto.setCantidad(reserva.getCantidad());
        dto.setFechaReserva(reserva.getFechaReserva());
        dto.setPrecioUnitario(reserva.getPrecioUnitario());
        dto.setPrecioTotal(reserva.getPrecioTotal());
        dto.setComentarios(reserva.getComentarios());
        dto.setEstado(reserva.getEstado());
        dto.setActivo(reserva.getActivo());
        dto.setFechaCreacion(reserva.getFechaCreacion());
        
        // Incluir información del usuario
        if (reserva.getUsuario() != null) {
            com.partymap.backend.DTO.UsuarioResponseDTO usuarioDTO = new com.partymap.backend.DTO.UsuarioResponseDTO();
            usuarioDTO.setId(reserva.getUsuario().getId());
            usuarioDTO.setNombre(reserva.getUsuario().getNombre());
            usuarioDTO.setEmail(reserva.getUsuario().getEmail());
            usuarioDTO.setTipoUsuario(reserva.getUsuario().getTipoUsuario());
            usuarioDTO.setActivo(reserva.getUsuario().getActivo());
            usuarioDTO.setFechaCreacion(reserva.getUsuario().getFechaCreacion());
            dto.setUsuario(usuarioDTO);
        }
        
        // Incluir información del evento
        if (reserva.getEvento() != null) {
            com.partymap.backend.DTO.EventoResponseDTO eventoDTO = new com.partymap.backend.DTO.EventoResponseDTO();
            eventoDTO.setId(reserva.getEvento().getId());
            eventoDTO.setNombre(reserva.getEvento().getNombre());
            eventoDTO.setDescripcion(reserva.getEvento().getDescripcion());
            eventoDTO.setFecha(reserva.getEvento().getFecha());
            eventoDTO.setCapacidadMaxima(reserva.getEvento().getCapacidadMaxima());
            eventoDTO.setPrecioEntrada(reserva.getEvento().getPrecioEntrada());
            eventoDTO.setImagenUrl(reserva.getEvento().getImagenUrl());
            eventoDTO.setActivo(reserva.getEvento().getActivo());
            eventoDTO.setFechaCreacion(reserva.getEvento().getFechaCreacion());
            eventoDTO.setCuposDisponibles(reserva.getEvento().getCuposDisponibles());
            eventoDTO.setDisponible(reserva.getEvento().isDisponible());
            eventoDTO.setEventoPasado(reserva.getEvento().isEventoPasado());
            eventoDTO.setEventoProximo(reserva.getEvento().isEventoProximo());
            dto.setEvento(eventoDTO);
        }
        
        return dto;
    }

    /**
     * Convierte un ReservaDTO a Reserva
     */
    private Reserva convertToEntity(ReservaDTO dto) {
        Reserva reserva = new Reserva();
        reserva.setId(dto.getId());
        reserva.setCantidad(dto.getCantidad());
        reserva.setPrecioUnitario(dto.getPrecioUnitario());
        reserva.setComentarios(dto.getComentarios());
        
        // Buscar y asignar usuario por ID
        if (dto.getUsuarioId() != null) {
            Optional<Usuario> usuario = usuarioRepository.findById(dto.getUsuarioId());
            if (usuario.isEmpty()) {
                throw new IllegalArgumentException("Usuario no encontrado con ID: " + dto.getUsuarioId());
            }
            reserva.setUsuario(usuario.get());
        }
        
        // Buscar y asignar evento por ID
        if (dto.getEventoId() != null) {
            Optional<Evento> evento = eventoRepository.findById(dto.getEventoId());
            if (evento.isEmpty()) {
                throw new IllegalArgumentException("Evento no encontrado con ID: " + dto.getEventoId());
            }
            reserva.setEvento(evento.get());
        }
        
        return reserva;
    }
}
