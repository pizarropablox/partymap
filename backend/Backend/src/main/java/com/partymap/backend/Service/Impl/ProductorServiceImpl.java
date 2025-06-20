package com.partymap.backend.Service.Impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.partymap.backend.Exceptions.NotFoundException;
import com.partymap.backend.Model.Productor;
import com.partymap.backend.Repository.ProductorRepository;
import com.partymap.backend.Service.ProductorService;

@Service
public class ProductorServiceImpl implements ProductorService {


     private final ProductorRepository productorRepository;

    public ProductorServiceImpl(ProductorRepository productorRepository){
        this.productorRepository = productorRepository;
    }


    //implementar los metodos de la interfase de productor

    // Método para obtener todos los productores
    @Override
    public List<Productor>getAllProductor(){
        return productorRepository.findAll();
    }


    //metodo para obtener reserva por su ID
    @Override
    public Optional<Productor> getProductorById(String rut) {
        return productorRepository.findById(rut);
    }


    //metodo para crear reserva
    @Override
    public Productor createProductor(Productor productor) throws IOException{
        return productorRepository.save(productor);
    }


    //metodo para actualizar reserva
    @Override
    public Productor updateProductor (String rut,Productor productor){
        if(!productorRepository.existsById(rut)){
             throw new NotFoundException("Productor no encontrado en Rut:"+ productor.getRut());
        }
        productor.setRut(rut);
        return productorRepository.save(productor);
    }


    // metodo para elimar una reserva
    @Override
    public void deleteProductor(Productor productor) throws IOException{
          if(!productorRepository.existsById(productor.getRut())){
             throw new NotFoundException("Productor no encontrado en ID:"+ productor.getRut());
        }
        productorRepository.deleteById(productor.getRut());
    }

}
