package com.partymap.backend.Service;


import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.partymap.backend.Model.Reserva;

public interface ReservaService {

      //listar todas las reservas
    List<Reserva> getAllreservas();

     //buscar reserva por id
    Optional<Reserva> getReservaById(Long id);

    //crear Reserva
    Reserva createReserva(Reserva id) throws IOException;

    //actualizar paciente
    Reserva updateReserva(Long id,Reserva reserva);

     //eliminar reserva
    void deleteReserva(Reserva reserva) throws IOException;

}
