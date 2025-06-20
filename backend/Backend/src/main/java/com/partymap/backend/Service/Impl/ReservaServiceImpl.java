package com.partymap.backend.Service.Impl;

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

import com.partymap.backend.Exceptions.NotFoundException;
import com.partymap.backend.Model.EstadoReserva;
import com.partymap.backend.Model.Reserva;
import com.partymap.backend.Repository.ReservaRepository;
import com.partymap.backend.Service.ReservaService;

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
     * Obtiene todas las reservas del sistema
     */
    @Override
    public List<Reserva> getAllreservas() {
        return reservaRepository.findAll();
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
        // Validar que la reserva tenga los datos requeridos
        if (reserva.getUsuario() == null) {
            throw new IllegalArgumentException("La reserva debe tener un usuario asociado");
        }
        if (reserva.getEvento() == null) {
            throw new IllegalArgumentException("La reserva debe tener un evento asociado");
        }
        if (reserva.getCantidad() == null || reserva.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        
        // Verificar que el evento esté disponible
        if (!reserva.getEvento().isDisponible()) {
            throw new IllegalArgumentException("El evento no está disponible para reservas");
        }
        
        // Verificar cupos disponibles
        if (reserva.getEvento().getCuposDisponibles() < reserva.getCantidad()) {
            throw new IllegalArgumentException("No hay suficientes cupos disponibles para la cantidad solicitada");
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
     * Elimina una reserva del sistema
     */
    @Override
    public void deleteReserva(Reserva reserva) throws IOException {
        if (!reservaRepository.existsById(reserva.getId())) {
            throw new NotFoundException("Reserva no encontrada con ID: " + reserva.getId());
        }
        reservaRepository.deleteById(reserva.getId());
    }

    /**
     * Obtiene todas las reservas de un usuario específico
     */
    @Override
    public List<Reserva> getReservasByUsuarioId(Long usuarioId) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getUsuario().getId().equals(usuarioId))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las reservas de un evento específico
     */
    @Override
    public List<Reserva> getReservasByEventoId(Long eventoId) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getEvento().getId().equals(eventoId))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas activas (estado RESERVADA)
     */
    @Override
    public List<Reserva> getReservasActivas() {
        return reservaRepository.findAll().stream()
                .filter(Reserva::isActiva)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas canceladas (estado CANCELADA)
     */
    @Override
    public List<Reserva> getReservasCanceladas() {
        return reservaRepository.findAll().stream()
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
     * Obtiene reservas por rango de fechas
     */
    @Override
    public List<Reserva> getReservasPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getFechaReserva().isAfter(fechaInicio) && 
                                 reserva.getFechaReserva().isBefore(fechaFin))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas de un usuario en un rango de fechas
     */
    @Override
    public List<Reserva> getReservasUsuarioPorRangoFechas(Long usuarioId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getUsuario().getId().equals(usuarioId))
                .filter(reserva -> reserva.getFechaReserva().isAfter(fechaInicio) && 
                                 reserva.getFechaReserva().isBefore(fechaFin))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas de un evento en un rango de fechas
     */
    @Override
    public List<Reserva> getReservasEventoPorRangoFechas(Long eventoId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getEvento().getId().equals(eventoId))
                .filter(reserva -> reserva.getFechaReserva().isAfter(fechaInicio) && 
                                 reserva.getFechaReserva().isBefore(fechaFin))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas con precio total mayor a un valor específico
     */
    @Override
    public List<Reserva> getReservasPorPrecioMinimo(BigDecimal precioMinimo) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getPrecioTotal().compareTo(precioMinimo) > 0)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas con precio total menor a un valor específico
     */
    @Override
    public List<Reserva> getReservasPorPrecioMaximo(BigDecimal precioMaximo) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getPrecioTotal().compareTo(precioMaximo) < 0)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas por cantidad de entradas
     */
    @Override
    public List<Reserva> getReservasPorCantidad(Integer cantidad) {
        return reservaRepository.findAll().stream()
                .filter(reserva -> reserva.getCantidad().equals(cantidad))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas con cantidad mayor a un valor específico
     */
    @Override
    public List<Reserva> getReservasPorCantidadMinima(Integer cantidadMinima) {
        return reservaRepository.findAll().stream()
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
     * Obtiene estadísticas completas de reservas
     */
    @Override
    public Object getEstadisticasCompletas() {
        List<Reserva> todasLasReservas = reservaRepository.findAll();
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
