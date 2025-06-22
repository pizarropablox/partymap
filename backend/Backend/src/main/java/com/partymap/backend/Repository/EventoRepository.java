package com.partymap.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.partymap.backend.Model.Evento;

/**
 * Repositorio para la entidad Evento.
 * Proporciona operaciones básicas de persistencia para eventos.
 */
public interface EventoRepository extends JpaRepository<Evento, Long> {

}
