package com.partymap.backend.Service.Impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.partymap.backend.Exceptions.NotFoundException;
import com.partymap.backend.Model.Reserva;
import com.partymap.backend.Repository.ReservaRepository;
import com.partymap.backend.Service.ReservaService;



@Service
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;

    public ReservaServiceImpl(ReservaRepository reservaRepository){
        this.reservaRepository = reservaRepository;
    }


    //implementar los metodos de la interfas de reservaservice

    // Método para obtener todos las reservas
    @Override
    public List<Reserva>getAllreservas(){
        return reservaRepository.findAll();
    }


    //metodo para obtener reserva por su ID
    @Override
    public Optional<Reserva> getReservaById(Long id) {
        return reservaRepository.findById(id);
    }


    //metodo para crear reserva
    @Override
    public Reserva createReserva(Reserva reserva) throws IOException{
        return reservaRepository.save(reserva);
    }


    //metodo para actualizar reserva
    @Override
    public Reserva updateReserva (Long id,Reserva reserva){
        if(!reservaRepository.existsById(id)){
             throw new NotFoundException("Reserva no encontrado en ID:"+ reserva.getId());
        }
        reserva.setId(id);
        return reservaRepository.save(reserva);
    }


    // metodo para elimar una reserva
    @Override
    public void deleteReserva(Reserva reserva) throws IOException{
          if(!reservaRepository.existsById(reserva.getId())){
             throw new NotFoundException("Reserva no encontrado en ID:"+ reserva.getId());
        }
        reservaRepository.deleteById(reserva.getId());
    }

}
