package com.partymap.backend.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partymap.backend.exceptions.NotFoundException;
import com.partymap.backend.model.EstadoReserva;
import com.partymap.backend.model.Reserva;
import com.partymap.backend.repository.ReservaRepository;
import com.partymap.backend.service.ReservaService;

/**
 * Implementación del servicio de reservas.
 * Gestiona las operaciones CRUD de reservas y funcionalidades adicionales
 * aprovechando todas las capacidades del modelo Reserva.
 */
@Service
@Transactional
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;

    public ReservaServiceImpl(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    /**
     * Obtiene todas las reservas activas del sistema
     */
    @Override
    public List<Reserva> getAllreservas() {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getActivo() == 1)
                .collect(Collectors.toList());
    }

    /**
     * Busca una reserva por su ID
     */
    @Override
    public Optional<Reserva> getReservaById(Long id) {
        return reservaRepository.findById(id);
    }

    /**
     * Crea una nueva reserva con validaciones completas
     */
    @Override
    public Reserva createReserva(Reserva reserva) throws IOException {
        // Validar campos obligatorios no nulos
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }
        if (reserva.getUsuario() == null) {
            throw new IllegalArgumentException("La reserva debe tener un usuario asociado");
        }
        if (reserva.getEvento() == null) {
            throw new IllegalArgumentException("La reserva debe tener un evento asociado");
        }
        if (reserva.getCantidad() == null) {
            throw new IllegalArgumentException("La cantidad no puede ser nula");
        }
        if (reserva.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (reserva.getCantidad() > 50) {
            throw new IllegalArgumentException("La cantidad máxima permitida es 50 entradas por reserva");
        }
        
        // Verificar que el usuario esté activo
        if (reserva.getUsuario().getActivo() != 1) {
            throw new IllegalArgumentException("El usuario no está activo");
        }
        
        // Verificar que el evento esté activo
        if (reserva.getEvento().getActivo() != 1) {
            throw new IllegalArgumentException("El evento no está activo");
        }
        
        // Verificar que el evento no haya pasado
        if (reserva.getEvento().isEventoPasado()) {
            throw new IllegalArgumentException("El evento ya ha pasado");
        }
        
        // Verificar que el evento esté disponible
        if (!reserva.getEvento().isDisponible()) {
            throw new IllegalArgumentException("El evento no está disponible para reservas");
        }
        
        // Verificar cupos disponibles
        int cuposDisponibles = reserva.getEvento().getCuposDisponibles();
        if (cuposDisponibles < reserva.getCantidad()) {
            throw new IllegalArgumentException("No hay suficientes cupos disponibles. Cupos disponibles: " + cuposDisponibles + ", Cantidad solicitada: " + reserva.getCantidad());
        }
        
        // Verificar que el usuario no tenga ya una reserva activa para este evento
        List<Reserva> reservasExistentes = getReservasByUsuarioId(reserva.getUsuario().getId());
        boolean yaTieneReserva = reservasExistentes.stream()
                .anyMatch(r -> r.getEvento().getId().equals(reserva.getEvento().getId()) && r.isActiva());
        
        if (yaTieneReserva) {
            throw new IllegalArgumentException("El usuario ya tiene una reserva activa para este evento");
        }
        
        // Verificar límite de 5 reservas ACTIVAS por usuario por evento (NO incluir canceladas)
        long totalReservasActivasUsuarioEvento = reservasExistentes.stream()
                .filter(r -> r.getEvento().getId().equals(reserva.getEvento().getId()) && r.isActiva())
                .count();
        
        if (totalReservasActivasUsuarioEvento >= 5) {
            throw new IllegalArgumentException("El usuario ya ha alcanzado el límite máximo de 5 reservas activas para este evento");
        }
        
        // Establecer precio unitario si no se especifica
        if (reserva.getPrecioUnitario() == null) {
            reserva.setPrecioUnitario(reserva.getEvento().getPrecioEntrada());
        }
        
        // Validar que el precio unitario no sea negativo
        if (reserva.getPrecioUnitario() != null && reserva.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
        
        // El modelo Reserva maneja automáticamente:
        // - Establecimiento de fecha de reserva (@PrePersist)
        // - Establecimiento de estado por defecto (@PrePersist)
        // - Cálculo automático del precio total (@PrePersist)
        
        return reservaRepository.save(reserva);
    }

    /**
     * Actualiza una reserva existente con recálculo automático de precios
     */
    @Override
    public Reserva updateReserva(Long id, Reserva reserva) {
        if (!reservaRepository.existsById(id)) {
            throw new NotFoundException("Reserva no encontrada con ID: " + id);
        }
        
        // Validar que la reserva existente esté activa para modificaciones
        Optional<Reserva> reservaExistente = reservaRepository.findById(id);
        if (reservaExistente.isPresent() && !reservaExistente.get().isActiva()) {
            throw new IllegalArgumentException("No se puede modificar una reserva cancelada");
        }
        
        // Si se está cambiando la cantidad, verificar cupos disponibles
        if (reserva.getCantidad() != null && reserva.getCantidad() > 0) {
            int cuposDisponibles = reserva.getEvento().getCuposDisponibles();
            int cantidadActual = reservaExistente.get().getCantidad();
            int diferencia = reserva.getCantidad() - cantidadActual;
            
            if (diferencia > 0 && cuposDisponibles < diferencia) {
                throw new IllegalArgumentException("No hay suficientes cupos disponibles para la nueva cantidad");
            }
        }
        
        reserva.setId(id);
        // El modelo Reserva recalcula automáticamente el precio total en @PreUpdate
        return reservaRepository.save(reserva);
    }

    /**
     * Elimina una reserva del sistema (soft delete)
     */
    @Override
    public void deleteReserva(Reserva reserva) throws IOException {
        if (!reservaRepository.existsById(reserva.getId())) {
            throw new NotFoundException("Reserva no encontrada con ID: " + reserva.getId());
        }
        
        // Soft delete: cambiar estado activo a 0
        reserva.setActivo(0);
        reservaRepository.save(reserva);
    }

    /**
     * Obtiene todas las reservas activas de un usuario específico
     */
    @Override
    public List<Reserva> getReservasByUsuarioId(Long usuarioId) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getActivo() == 1)
                .filter(reserva -> reserva.getUsuario().getId().equals(usuarioId))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las reservas activas de un evento específico
     */
    @Override
    public List<Reserva> getReservasByEventoId(Long eventoId) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getActivo() == 1)
                .filter(reserva -> reserva.getEvento().getId().equals(eventoId))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas activas (estado RESERVADA y activo = 1)
     */
    @Override
    public List<Reserva> getReservasActivas() {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getActivo() == 1)
                .filter(Reserva::isActiva)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas canceladas (estado CANCELADA y activo = 1)
     */
    @Override
    public List<Reserva> getReservasCanceladas() {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getActivo() == 1)
                .filter(Reserva::isCancelada)
                .collect(Collectors.toList());
    }

    /**
     * Cancela una reserva cambiando su estado a CANCELADA
     */
    @Override
    public Reserva cancelarReserva(Long id) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            
            // Verificar que la reserva esté activa
            if (!reserva.isActiva()) {
                throw new IllegalArgumentException("No se puede cancelar una reserva que ya no está activa");
            }
            
            // Usar el método del modelo para cancelar
            reserva.cancelar();
            return reservaRepository.save(reserva);
        } else {
            throw new NotFoundException("Reserva no encontrada con ID: " + id);
        }
    }

    /**
     * Reactiva una reserva cancelada cambiando su estado a RESERVADA
     */
    @Override
    public Reserva reactivarReserva(Long id) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            
            // Verificar que la reserva esté cancelada
            if (!reserva.isCancelada()) {
                throw new IllegalArgumentException("No se puede reactivar una reserva que no está cancelada");
            }
            
            // Verificar que el evento aún esté disponible
            if (!reserva.getEvento().isDisponible()) {
                throw new IllegalArgumentException("No se puede reactivar la reserva porque el evento no está disponible");
            }
            
            // Verificar cupos disponibles
            if (reserva.getEvento().getCuposDisponibles() < reserva.getCantidad()) {
                throw new IllegalArgumentException("No hay suficientes cupos disponibles para reactivar la reserva");
            }
            
            // Reactivar la reserva
            reserva.setEstado(EstadoReserva.RESERVADA);
            return reservaRepository.save(reserva);
        } else {
            throw new NotFoundException("Reserva no encontrada con ID: " + id);
        }
    }

    /**
     * Obtiene el precio total de una reserva específica
     */
    @Override
    public BigDecimal getPrecioTotalReserva(Long id) {
        Optional<Reserva> reserva = reservaRepository.findById(id);
        if (reserva.isPresent()) {
            return reserva.get().getPrecioTotal();
        } else {
            throw new NotFoundException("Reserva no encontrada con ID: " + id);
        }
    }

    /**
     * Obtiene reservas activas por rango de fechas
     */
    @Override
    public List<Reserva> getReservasPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getActivo() == 1)
                .filter(reserva -> reserva.getFechaReserva().isAfter(fechaInicio) && 
                                 reserva.getFechaReserva().isBefore(fechaFin))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas activas de un usuario en un rango de fechas
     */
    @Override
    public List<Reserva> getReservasUsuarioPorRangoFechas(Long usuarioId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getActivo() == 1)
                .filter(reserva -> reserva.getUsuario().getId().equals(usuarioId))
                .filter(reserva -> reserva.getFechaReserva().isAfter(fechaInicio) && 
                                 reserva.getFechaReserva().isBefore(fechaFin))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas activas de un evento en un rango de fechas
     */
    @Override
    public List<Reserva> getReservasEventoPorRangoFechas(Long eventoId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getActivo() == 1)
                .filter(reserva -> reserva.getEvento().getId().equals(eventoId))
                .filter(reserva -> reserva.getFechaReserva().isAfter(fechaInicio) && 
                                 reserva.getFechaReserva().isBefore(fechaFin))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas activas con precio total mayor a un valor específico
     */
    @Override
    public List<Reserva> getReservasPorPrecioMinimo(BigDecimal precioMinimo) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getActivo() == 1)
                .filter(reserva -> reserva.getPrecioTotal().compareTo(precioMinimo) > 0)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas activas con precio total menor a un valor específico
     */
    @Override
    public List<Reserva> getReservasPorPrecioMaximo(BigDecimal precioMaximo) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getActivo() == 1)
                .filter(reserva -> reserva.getPrecioTotal().compareTo(precioMaximo) < 0)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas activas por cantidad de entradas
     */
    @Override
    public List<Reserva> getReservasPorCantidad(Integer cantidad) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getActivo() == 1)
                .filter(reserva -> reserva.getCantidad().equals(cantidad))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas activas con cantidad mayor a un valor específico
     */
    @Override
    public List<Reserva> getReservasPorCantidadMinima(Integer cantidadMinima) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getActivo() == 1)
                .filter(reserva -> reserva.getCantidad() > cantidadMinima)
                .collect(Collectors.toList());
    }

    /**
     * Verifica si una reserva está activa
     */
    @Override
    public boolean isReservaActiva(Long id) {
        Optional<Reserva> reserva = reservaRepository.findById(id);
        return reserva.map(Reserva::isActiva).orElse(false);
    }

    /**
     * Verifica si una reserva está cancelada
     */
    @Override
    public boolean isReservaCancelada(Long id) {
        Optional<Reserva> reserva = reservaRepository.findById(id);
        return reserva.map(Reserva::isCancelada).orElse(false);
    }

    /**
     * Obtiene estadísticas completas de reservas activas
     */
    @Override
    public Object getEstadisticasCompletas() {
        List<Reserva> todasLasReservas = getAllreservas(); // Solo reservas activas
        List<Reserva> reservasActivas = getReservasActivas();
        List<Reserva> reservasCanceladas = getReservasCanceladas();
        
        // Estadísticas básicas
        long totalReservas = todasLasReservas.size();
        long reservasActivasCount = reservasActivas.size();
        long reservasCanceladasCount = reservasCanceladas.size();
        
        // Estadísticas de precios
        BigDecimal totalIngresos = reservasActivas.stream()
                .map(Reserva::getPrecioTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal promedioPrecio = totalReservas > 0 ? 
                totalIngresos.divide(BigDecimal.valueOf(totalReservas), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO;
        
        BigDecimal precioMaximo = todasLasReservas.stream()
                .map(Reserva::getPrecioTotal)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal precioMinimo = todasLasReservas.stream()
                .map(Reserva::getPrecioTotal)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        // Estadísticas de cantidad
        int totalEntradas = todasLasReservas.stream()
                .mapToInt(Reserva::getCantidad)
                .sum();
        
        double promedioEntradas = totalReservas > 0 ? 
                (double) totalEntradas / totalReservas : 0.0;
        
        int maxEntradas = todasLasReservas.stream()
                .mapToInt(Reserva::getCantidad)
                .max()
                .orElse(0);
        
        int minEntradas = todasLasReservas.stream()
                .mapToInt(Reserva::getCantidad)
                .min()
                .orElse(0);
        
        // Estadísticas por estado
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalReservas", totalReservas);
        estadisticas.put("reservasActivas", reservasActivasCount);
        estadisticas.put("reservasCanceladas", reservasCanceladasCount);
        estadisticas.put("porcentajeActivas", totalReservas > 0 ? 
                (double) reservasActivasCount / totalReservas * 100 : 0.0);
        estadisticas.put("porcentajeCanceladas", totalReservas > 0 ? 
                (double) reservasCanceladasCount / totalReservas * 100 : 0.0);
        
        // Estadísticas de precios
        estadisticas.put("totalIngresos", totalIngresos);
        estadisticas.put("promedioPrecio", promedioPrecio);
        estadisticas.put("precioMaximo", precioMaximo);
        estadisticas.put("precioMinimo", precioMinimo);
        
        // Estadísticas de entradas
        estadisticas.put("totalEntradas", totalEntradas);
        estadisticas.put("promedioEntradas", promedioEntradas);
        estadisticas.put("maxEntradas", maxEntradas);
        estadisticas.put("minEntradas", minEntradas);
        
        return estadisticas;
    }
}
