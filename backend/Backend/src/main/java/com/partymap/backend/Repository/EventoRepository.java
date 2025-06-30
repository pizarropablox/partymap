package com.partymap.backend.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.partymap.backend.Model.Evento;

/**
 * Repositorio para la entidad Evento.
 * Proporciona operaciones básicas de persistencia para eventos.
 */
public interface EventoRepository extends JpaRepository<Evento, Long> {

    /**
     * Busca eventos por ID de usuario
     * @param usuarioId ID del usuario
     * @return Lista de eventos del usuario
     */
    List<Evento> findByUsuarioId(Long usuarioId);
}
