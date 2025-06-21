package com.partymap.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.partymap.backend.Model.Productor;

public interface ProductorRepository extends JpaRepository<Productor, Long> {

    /**
     * Busca un productor por RUT
     */
    Optional<Productor> findByRut(String rut);

    /**
     * Verifica si existe un productor con el RUT especificado
     */
    boolean existsByRut(String rut);

    /**
     * Busca productores por nombre de empresa (ignorando mayúsculas/minúsculas)
     */
    @Query("SELECT p FROM Productor p WHERE LOWER(p.nombreEmpresa) LIKE LOWER(CONCAT('%', :nombreEmpresa, '%'))")
    List<Productor> findByNombreEmpresaContainingIgnoreCase(@Param("nombreEmpresa") String nombreEmpresa);

    /**
     * Busca productores activos
     */
    List<Productor> findByActivoTrue();

    /**
     * Busca productores por ID de usuario
     */
    @Query("SELECT p FROM Productor p WHERE p.usuario.id = :usuarioId")
    Optional<Productor> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Verifica si existe un productor asociado al usuario especificado
     */
    @Query("SELECT COUNT(p) > 0 FROM Productor p WHERE p.usuario.id = :usuarioId")
    boolean existsByUsuarioId(@Param("usuarioId") Long usuarioId);
}
