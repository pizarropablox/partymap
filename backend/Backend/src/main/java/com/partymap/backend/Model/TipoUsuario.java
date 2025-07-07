package com.partymap.backend.model;

/**
 * Enumeración que define los tipos de usuarios en el sistema PartyMap.
 * Cada tipo tiene diferentes permisos y funcionalidades:
 * - CLIENTE: Puede ver eventos y hacer reservas
 * - PRODUCTOR: Puede crear y gestionar eventos
 * - ADMINISTRADOR: Tiene acceso completo al sistema
 */
public enum TipoUsuario {
    CLIENTE,
    PRODUCTOR,
    ADMINISTRADOR
} 