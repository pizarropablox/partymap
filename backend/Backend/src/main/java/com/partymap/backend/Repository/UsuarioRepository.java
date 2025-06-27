package com.partymap.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.partymap.backend.Model.TipoUsuario;
import com.partymap.backend.Model.Usuario;

/**
 * Repositorio para la entidad Usuario.
 * Proporciona métodos para acceder a la base de datos de usuarios.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por email
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca un usuario por Azure B2C ID
     */
    Optional<Usuario> findByAzureB2cId(String azureB2cId);

    /**
     * Verifica si existe un usuario con el email especificado
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un usuario con el Azure B2C ID especificado
     */
    boolean existsByAzureB2cId(String azureB2cId);

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