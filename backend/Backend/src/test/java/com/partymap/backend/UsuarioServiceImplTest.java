package com.partymap.backend;

import com.partymap.backend.Model.Usuario;
import com.partymap.backend.Repository.UsuarioRepository;
import com.partymap.backend.Service.Impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuarioServiceImplTest {

    private UsuarioRepository usuarioRepository;
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        usuarioService = new UsuarioServiceImpl(usuarioRepository);
    }

    @Test
    void deberiaRetornarListaUsuariosVacia() {
        when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());

        List<Usuario> usuarios = usuarioService.getAllUsuarios();

        assertNotNull(usuarios);
        assertTrue(usuarios.isEmpty());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    void deberiaCrearUsuarioCorrectamente() {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("Juan");

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        Usuario resultado = usuarioService.createUsuario(usuarioMock);

        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        verify(usuarioRepository, times(1)).save(usuarioMock);
    }
}
