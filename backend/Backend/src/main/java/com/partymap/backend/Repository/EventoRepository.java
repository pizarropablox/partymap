package com.partymap.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.partymap.backend.Model.Evento;

public interface  EventoRepository extends JpaRepository<Evento,Long> {

}
