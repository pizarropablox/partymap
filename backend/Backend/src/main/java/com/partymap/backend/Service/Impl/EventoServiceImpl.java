package com.partymap.backend.Service.Impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.partymap.backend.Exceptions.NotFoundException;
import com.partymap.backend.Model.Evento;
import com.partymap.backend.Model.Reserva;
import com.partymap.backend.Repository.EventoRepository;
import com.partymap.backend.Service.EventoService;



@Service
public class EventoServiceImpl implements EventoService{

   private final EventoRepository eventoRepository;

    public EventoServiceImpl(EventoRepository eventoRepository){
        this.eventoRepository = eventoRepository;
    }


    //implementar los metodos de la interfas de eventoservice

    // Método para obtener todos los eventos
    @Override
    public List<Evento>getAllEvento(){
        return eventoRepository.findAll();
    }


    //metodo para obtener reserva por su ID
    @Override
    public Optional<Evento> getEventoById(Long id) {
        return eventoRepository.findById(id);
    }


    //metodo para crear reserva
    @Override
    public Evento createEvento(Evento evento) throws IOException{
        return eventoRepository.save(evento);
    }


    //metodo para actualizar reserva
    @Override
    public Evento updateEvento (Long id,Evento evento){
        if(!eventoRepository.existsById(id)){
             throw new NotFoundException("Evento no encontrado en ID:"+ evento.getId());
        }
        evento.setId(id);
        return eventoRepository.save(evento);
    }


    // metodo para elimar una reserva
    @Override
    public void deleteEvento(Evento evento) throws IOException{
          if(!eventoRepository.existsById(evento.getId())){
             throw new NotFoundException("Evento no encontrado en ID:"+ evento.getId());
        }
        eventoRepository.deleteById(evento.getId());
    }

}
