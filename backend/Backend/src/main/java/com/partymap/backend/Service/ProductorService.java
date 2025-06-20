package com.partymap.backend.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.partymap.backend.Model.Productor;



public interface ProductorService {

     //listar todas los Eventos
    List<Productor> getAllProductor();

     //buscar Evento por id
    Optional<Productor> getProductorById(String rut);

    //crear Evento
    Productor createProductor(Productor rut) throws IOException;

    //actualizar Evento
    Productor updateProductor(String rut,Productor productor);

     //eliminar evento
    void deleteProductor(Productor productor) throws IOException;


}
