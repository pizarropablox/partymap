package com.partymap.backend.Service.Impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partymap.backend.Model.Usuario;
import com.partymap.backend.Repository.UsuarioRepository;
import com.partymap.backend.Service.UsuarioService;

/**
 * Implementación del servicio de usuarios.
 * Gestiona las operaciones CRUD de usuarios.
 */
@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Obtiene todos los usuarios activos del sistema
     */
    @Override
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getActivo() == 1)
                .collect(Collectors.toList());
    }

    /**
     * Busca un usuario por su ID
     */
    @Override
    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Busca un usuario por su email
     */
    @Override
    public Optional<Usuario> getUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Verifica si existe un usuario con el email especificado
     */
    @Override
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
} 