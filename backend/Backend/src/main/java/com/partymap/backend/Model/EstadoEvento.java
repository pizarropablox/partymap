package com.partymap.backend.Model;

/**
 * Enumeración que define los estados posibles de un evento en el sistema.
 * Controla el ciclo de vida del evento desde su creación hasta su finalización:
 * - BORRADOR: Evento en proceso de creación, no visible públicamente
 * - PUBLICADO: Evento visible y disponible para reservas
 * - EN_CURSO: Evento que está sucediendo actualmente
 * - FINALIZADO: Evento que ya terminó
 * - CANCELADO: Evento cancelado, no disponible para reservas
 */
public enum EstadoEvento {
    BORRADOR,
    PUBLICADO,
    EN_CURSO,
    FINALIZADO,
    CANCELADO
} 