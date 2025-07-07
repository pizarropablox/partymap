package com.partymap.backend.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.partymap.backend.model.Reserva;

/**
 * Interfaz de servicio para la gestión de reservas.
 * Proporciona métodos para operaciones CRUD de reservas y funcionalidades adicionales
 * que aprovechan las capacidades del modelo Reserva.
 */
public interface ReservaService {

    /**
     * Obtiene todas las reservas del sistema
     * @return Lista de todas las reservas
     */
    List<Reserva> getAllreservas();

    /**
     * Busca una reserva por su ID
     * @param id ID de la reserva a buscar
     * @return Optional con la reserva si existe
     */
    Optional<Reserva> getReservaById(Long id);

    /**
     * Crea una nueva reserva con validaciones completas
     * @param reserva Reserva a crear
     * @return Reserva creada con valores calculados automáticamente
     * @throws IOException Si hay error en la creación
     */
    Reserva createReserva(Reserva reserva) throws IOException;

    /**
     * Actualiza una reserva existente con recálculo automático de precios
     * @param id ID de la reserva a actualizar
     * @param reserva Datos actualizados de la reserva
     * @return Reserva actualizada con precio total recalculado
     */
    Reserva updateReserva(Long id, Reserva reserva);

    /**
     * Elimina una reserva del sistema
     * @param reserva Reserva a eliminar
     * @throws IOException Si hay error en la eliminación
     */
    void deleteReserva(Reserva reserva) throws IOException;

    /**
     * Obtiene todas las reservas de un usuario específico
     * @param usuarioId ID del usuario
     * @return Lista de reservas del usuario
     */
    List<Reserva> getReservasByUsuarioId(Long usuarioId);

    /**
     * Obtiene todas las reservas de un evento específico
     * @param eventoId ID del evento
     * @return Lista de reservas del evento
     */
    List<Reserva> getReservasByEventoId(Long eventoId);

    /**
     * Obtiene reservas activas (estado RESERVADA)
     * @return Lista de reservas activas
     */
    List<Reserva> getReservasActivas();

    /**
     * Obtiene reservas canceladas (estado CANCELADA)
     * @return Lista de reservas canceladas
     */
    List<Reserva> getReservasCanceladas();

    /**
     * Cancela una reserva cambiando su estado a CANCELADA
     * @param id ID de la reserva a cancelar
     * @return Reserva cancelada
     */
    Reserva cancelarReserva(Long id);

    /**
     * Reactiva una reserva cancelada cambiando su estado a RESERVADA
     * @param id ID de la reserva a reactivar
     * @return Reserva reactivada
     */
    Reserva reactivarReserva(Long id);

    /**
     * Obtiene el precio total de una reserva específica
     * @param id ID de la reserva
     * @return Precio total calculado
     */
    BigDecimal getPrecioTotalReserva(Long id);

    /**
     * Obtiene reservas por rango de fechas
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de reservas en el rango de fechas
     */
    List<Reserva> getReservasPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Obtiene reservas de un usuario en un rango de fechas
     * @param usuarioId ID del usuario
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de reservas del usuario en el rango de fechas
     */
    List<Reserva> getReservasUsuarioPorRangoFechas(Long usuarioId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Obtiene reservas de un evento en un rango de fechas
     * @param eventoId ID del evento
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de reservas del evento en el rango de fechas
     */
    List<Reserva> getReservasEventoPorRangoFechas(Long eventoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Obtiene reservas con precio total mayor a un valor específico
     * @param precioMinimo Precio mínimo para filtrar
     * @return Lista de reservas con precio total mayor al especificado
     */
    List<Reserva> getReservasPorPrecioMinimo(BigDecimal precioMinimo);

    /**
     * Obtiene reservas con precio total menor a un valor específico
     * @param precioMaximo Precio máximo para filtrar
     * @return Lista de reservas con precio total menor al especificado
     */
    List<Reserva> getReservasPorPrecioMaximo(BigDecimal precioMaximo);

    /**
     * Obtiene reservas por cantidad de entradas
     * @param cantidad Cantidad de entradas a buscar
     * @return Lista de reservas con la cantidad especificada
     */
    List<Reserva> getReservasPorCantidad(Integer cantidad);

    /**
     * Obtiene reservas con cantidad mayor a un valor específico
     * @param cantidadMinima Cantidad mínima de entradas
     * @return Lista de reservas con cantidad mayor a la especificada
     */
    List<Reserva> getReservasPorCantidadMinima(Integer cantidadMinima);

    /**
     * Verifica si una reserva está activa
     * @param id ID de la reserva
     * @return true si la reserva está activa, false en caso contrario
     */
    boolean isReservaActiva(Long id);

    /**
     * Verifica si una reserva está cancelada
     * @param id ID de la reserva
     * @return true si la reserva está cancelada, false en caso contrario
     */
    boolean isReservaCancelada(Long id);

    /**
     * Obtiene estadísticas completas de reservas
     * @return Objeto con estadísticas detalladas
     */
    Object getEstadisticasCompletas();
}
