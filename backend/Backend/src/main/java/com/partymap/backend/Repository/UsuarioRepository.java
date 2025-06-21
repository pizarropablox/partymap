package com.partymap.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.partymap.backend.Model.TipoUsuario;
import com.partymap.backend.Model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por email
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email especificado
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuarios por tipo
     */
    List<Usuario> findByTipoUsuario(TipoUsuario tipoUsuario);

    /**
     * Busca usuarios activos
     */
    List<Usuario> findByActivoTrue();

    /**
     * Busca usuarios por tipo y estado activo
     */
    List<Usuario> findByTipoUsuarioAndActivoTrue(TipoUsuario tipoUsuario);

    /**
     * Busca usuarios por nombre (ignorando mayúsculas/minúsculas)
     */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Usuario> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Busca usuarios por email (ignorando mayúsculas/minúsculas)
     */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<Usuario> findByEmailContainingIgnoreCase(@Param("email") String email);
} 