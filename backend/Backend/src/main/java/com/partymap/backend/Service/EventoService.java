package com.partymap.backend.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.partymap.backend.Model.Evento;


public interface EventoService {

    //listar todas los Eventos
    List<Evento> getAllEvento();

     //buscar Evento por id
    Optional<Evento> getEventoById(Long id);

    //crear Evento
    Evento createEvento(Evento id) throws IOException;

    //actualizar Evento
    Evento updateEvento(Long id,Evento evento);

     //eliminar evento
    void deleteEvento(Evento evento) throws IOException;


}
