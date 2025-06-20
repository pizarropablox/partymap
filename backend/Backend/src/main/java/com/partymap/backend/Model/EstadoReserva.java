package com.partymap.backend.Model;

/**
 * Enumeración que define los estados posibles de una reserva en el sistema.
 * Simplificado a dos estados básicos:
 * - RESERVADA: Reserva activa y válida
 * - CANCELADA: Reserva cancelada por el usuario o sistema
 */
public enum EstadoReserva {
    RESERVADA,
    CANCELADA
} 