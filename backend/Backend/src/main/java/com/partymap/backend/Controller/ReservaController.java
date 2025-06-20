package com.partymap.backend.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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

import com.partymap.backend.DTO.ReservaDTO;
import com.partymap.backend.DTO.ReservaResponseDTO;
import com.partymap.backend.Exceptions.NotFoundException;
import com.partymap.backend.Model.Reserva;
import com.partymap.backend.Service.ReservaService;

/**
 * Controlador REST para la gestión de reservas.
 * Proporciona endpoints para operaciones CRUD de reservas y funcionalidades adicionales
 * que aprovechan todas las capacidades del servicio mejorado.
 */
@RestController
@CrossOrigin
@RequestMapping("/reserva")
public class ReservaController {

    private final ReservaService reservaService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    /**
     * Obtiene todas las reservas del sistema
     * GET /reserva
     */
    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> getAllReservas() {
        List<Reserva> reservas = reservaService.getAllreservas();
        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Obtiene una reserva por su ID
     * GET /reserva/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> getReservaById(@PathVariable Long id) {
        Optional<Reserva> reserva = reservaService.getReservaById(id);
        if (reserva.isPresent()) {
            return ResponseEntity.ok(convertToResponseDTO(reserva.get()));
        } else {
            throw new NotFoundException("Reserva no encontrada con ID: " + id);
        }
    }

    /**
     * Crea una nueva reserva
     * POST /reserva
     */
    @PostMapping
    public ResponseEntity<ReservaResponseDTO> createReserva(@RequestBody ReservaDTO reservaDTO) {
        try {
            Reserva reserva = convertToEntity(reservaDTO);
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
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> updateReserva(
            @PathVariable Long id,
            @RequestBody ReservaDTO reservaDTO) {
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
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReserva(@PathVariable Long id) {
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
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasByUsuario(@PathVariable Long usuarioId) {
        List<Reserva> reservas = reservaService.getReservasByUsuarioId(usuarioId);
        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Obtiene todas las reservas de un evento específico
     * GET /reserva/evento/{eventoId}
     */
    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasByEvento(@PathVariable Long eventoId) {
        List<Reserva> reservas = reservaService.getReservasByEventoId(eventoId);
        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Obtiene todas las reservas activas
     * GET /reserva/activas
     */
    @GetMapping("/activas")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasActivas() {
        List<Reserva> reservas = reservaService.getReservasActivas();
        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Obtiene todas las reservas canceladas
     * GET /reserva/canceladas
     */
    @GetMapping("/canceladas")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasCanceladas() {
        List<Reserva> reservas = reservaService.getReservasCanceladas();
        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Cancela una reserva
     * PUT /reserva/{id}/cancelar
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponseDTO> cancelarReserva(@PathVariable Long id) {
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
     */
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<ReservaResponseDTO> reactivarReserva(@PathVariable Long id) {
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
     */
    @GetMapping("/{id}/precio-total")
    public ResponseEntity<BigDecimal> getPrecioTotal(@PathVariable Long id) {
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
     */
    @GetMapping("/rango-fechas")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasPorRangoFechas(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
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
     */
    @GetMapping("/usuario/{usuarioId}/rango-fechas")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasUsuarioPorRangoFechas(
            @PathVariable Long usuarioId,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
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
     */
    @GetMapping("/evento/{eventoId}/rango-fechas")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasEventoPorRangoFechas(
            @PathVariable Long eventoId,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
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
     */
    @GetMapping("/precio-minimo")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasPorPrecioMinimo(
            @RequestParam BigDecimal precioMinimo) {
        List<Reserva> reservas = reservaService.getReservasPorPrecioMinimo(precioMinimo);
        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Obtiene reservas con precio total menor a un valor específico
     * GET /reserva/precio-maximo
     */
    @GetMapping("/precio-maximo")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasPorPrecioMaximo(
            @RequestParam BigDecimal precioMaximo) {
        List<Reserva> reservas = reservaService.getReservasPorPrecioMaximo(precioMaximo);
        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Obtiene reservas por cantidad de entradas
     * GET /reserva/cantidad/{cantidad}
     */
    @GetMapping("/cantidad/{cantidad}")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasPorCantidad(@PathVariable Integer cantidad) {
        List<Reserva> reservas = reservaService.getReservasPorCantidad(cantidad);
        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Obtiene reservas con cantidad mayor a un valor específico
     * GET /reserva/cantidad-minima
     */
    @GetMapping("/cantidad-minima")
    public ResponseEntity<List<ReservaResponseDTO>> getReservasPorCantidadMinima(
            @RequestParam Integer cantidadMinima) {
        List<Reserva> reservas = reservaService.getReservasPorCantidadMinima(cantidadMinima);
        List<ReservaResponseDTO> reservasDTO = reservas.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservasDTO);
    }

    /**
     * Verifica si una reserva está activa
     * GET /reserva/{id}/activa
     */
    @GetMapping("/{id}/activa")
    public ResponseEntity<Boolean> isReservaActiva(@PathVariable Long id) {
        boolean isActiva = reservaService.isReservaActiva(id);
        return ResponseEntity.ok(isActiva);
    }

    /**
     * Verifica si una reserva está cancelada
     * GET /reserva/{id}/cancelada
     */
    @GetMapping("/{id}/cancelada")
    public ResponseEntity<Boolean> isReservaCancelada(@PathVariable Long id) {
        boolean isCancelada = reservaService.isReservaCancelada(id);
        return ResponseEntity.ok(isCancelada);
    }

    /**
     * Busca reservas con filtros avanzados
     * GET /reserva/buscar
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
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Object> getEstadisticas() {
        Object estadisticas = reservaService.getEstadisticasCompletas();
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtiene estadísticas básicas de reservas (método anterior mantenido por compatibilidad)
     * GET /reserva/estadisticas-basicas
     */
    @GetMapping("/estadisticas-basicas")
    public ResponseEntity<Object> getEstadisticasBasicas() {
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
        dto.setFechaModificacion(reserva.getFechaModificacion());
        
        // TODO: Convertir usuario y evento a DTOs si es necesario
        // Por ahora se dejan como null para evitar dependencias circulares
        
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
        
        // TODO: Buscar y asignar usuario y evento por ID
        // Por ahora se dejan como null para que el servicio los maneje
        
        return reserva;
    }
}
