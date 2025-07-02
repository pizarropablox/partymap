package com.partymap.backend.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.partymap.backend.Model.Ubicacion;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {

    /**
     * Busca ubicaciones por comuna (ignorando mayúsculas/minúsculas)
     */
    @Query("SELECT u FROM Ubicacion u WHERE LOWER(u.comuna) LIKE LOWER(CONCAT('%', :comuna, '%'))")
    List<Ubicacion> findByComunaContainingIgnoreCase(@Param("comuna") String comuna);

    /**
     * Busca ubicaciones por dirección (ignorando mayúsculas/minúsculas)
     */
    @Query("SELECT u FROM Ubicacion u WHERE LOWER(u.direccion) LIKE LOWER(CONCAT('%', :direccion, '%'))")
    List<Ubicacion> findByDireccionContainingIgnoreCase(@Param("direccion") String direccion);

    /**
     * Busca ubicaciones por comuna exacta
     */
    List<Ubicacion> findByComunaIgnoreCase(String comuna);

    /**
     * Verifica si existe una ubicación con la misma dirección y comuna
     */
    boolean existsByDireccionAndComunaIgnoreCase(String direccion, String comuna);
}
