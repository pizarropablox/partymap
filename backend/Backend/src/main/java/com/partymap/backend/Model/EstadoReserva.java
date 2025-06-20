package com.partymap.backend.Model;

/**
 * Enumeración que define los estados posibles de una reserva en el sistema.
 * Controla el flujo de la reserva desde su creación hasta su finalización:
 * - PENDIENTE: Reserva creada pero aún no confirmada
 * - CONFIRMADA: Reserva aprobada y válida
 * - CANCELADA: Reserva cancelada por el usuario o sistema
 * - COMPLETADA: Reserva utilizada en el evento
 */
public enum EstadoReserva {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA,
    COMPLETADA
} 