package com.partymap.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.partymap.backend.Model.Ubicacion;

public interface  UbicacionRepository extends JpaRepository<Ubicacion, Long> {

}
