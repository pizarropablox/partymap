package com.partymap.backend.controller;

import com.partymap.backend.model.TipoUsuario;
import com.partymap.backend.model.Usuario;
import com.partymap.backend.service.UsuarioService;
import com.partymap.backend.dto.UsuarioResponseDTO;
import com.partymap.backend.controller.UsuarioController;
import com.partymap.backend.config.SecurityUtils;
import com.partymap.backend.exceptions.NotFoundException;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UsuarioController usuarioController;

    private Usuario admin;
    private Usuario cliente;
    private Usuario productor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        admin = new Usuario();
        admin.setId(1L);
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        admin.setEmail("admin@test.com");

        cliente = new Usuario();
        cliente.setId(2L);
        cliente.setTipoUsuario(TipoUsuario.CLIENTE);
        cliente.setEmail("cliente@test.com");

        productor = new Usuario();
        productor.setId(3L);
        productor.setTipoUsuario(TipoUsuario.PRODUCTOR);
        productor.setEmail("prod@test.com");
        productor.setRutProductor("12345678-9");
    }


    @Test
    void testGetAllUsuariosComoAdmin() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(usuarioService.getAllUsuarios()).thenReturn(List.of(admin, cliente, productor));

        ResponseEntity<List<UsuarioResponseDTO>> response = usuarioController.getAllUsuarios();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(3, response.getBody().size());
    }

    // FUNCIONANDO

    @Test
    void testGetAllUsuariosComoClienteDebeDar403() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(cliente));

        ResponseEntity<List<UsuarioResponseDTO>> response = usuarioController.getAllUsuarios();

        assertEquals(403, response.getStatusCodeValue());
    }

    // FUNCIONANDO

        
    @Test
    void testGetUsuarioByIdComoClienteMismoId() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(cliente));
        when(usuarioService.getUsuarioById(2L)).thenReturn(Optional.of(cliente));

        ResponseEntity<UsuarioResponseDTO> response = usuarioController.getUsuarioById(2L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("cliente@test.com", response.getBody().getEmail());
    }

    // FUNCIONANDO

    @Test
    void testGetUsuarioByIdComoClienteDistintoIdDebeDar403() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(cliente));

        ResponseEntity<UsuarioResponseDTO> response = usuarioController.getUsuarioById(3L);

        assertEquals(403, response.getStatusCodeValue());
    }

    // FUNCIONANDO

        @Test
    void testGetUsuarioByEmail_AdminPuedeBuscarCualquiera() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(usuarioService.getUsuarioByEmail("cliente@test.com")).thenReturn(Optional.of(cliente));

        ResponseEntity<UsuarioResponseDTO> response = usuarioController.getUsuarioByEmail("cliente@test.com");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("cliente@test.com", response.getBody().getEmail());
    }

    @Test
    void testGetUsuarioByEmail_ClienteOtroEmail_Denegado() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(cliente));

        ResponseEntity<UsuarioResponseDTO> response = usuarioController.getUsuarioByEmail("otro@test.com");

        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    void testGetProductorByRut_AdminPuedeBuscarCualquiera() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(usuarioService.getProductorByRut("12345678-9")).thenReturn(Optional.of(productor));

        ResponseEntity<UsuarioResponseDTO> response = usuarioController.getProductorByRut("12345678-9");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("prod@test.com", response.getBody().getEmail());
    }

    @Test
    void testGetProductorByRut_ClienteNoPuedeAcceder() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(cliente));

        ResponseEntity<UsuarioResponseDTO> response = usuarioController.getProductorByRut("12345678-9");

        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    void testExisteProductorPorRut_AdminPuedeVerificar() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(usuarioService.existsProductorByRut("12345678-9")).thenReturn(true);

        ResponseEntity<Boolean> response = usuarioController.existeProductorPorRut("12345678-9");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody());
    }

    @Test
    void testGetAllProductores_AdminRecibeLista() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(usuarioService.getAllProductores()).thenReturn(List.of(productor));

        ResponseEntity<List<UsuarioResponseDTO>> response = usuarioController.getAllProductores();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testBuscarProductores_AdminRecibeFiltradosPorNombre() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(usuarioService.getProductoresByNombre("prod")).thenReturn(List.of(productor));

        ResponseEntity<List<UsuarioResponseDTO>> response = usuarioController.buscarProductores("prod");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetCurrentUsuario_DevuelveUsuarioActual() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(cliente));

        ResponseEntity<UsuarioResponseDTO> response = usuarioController.getCurrentUsuario();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("cliente@test.com", response.getBody().getEmail());
    }

    @Test
    void testGetUsuariosByTipo_AdminRecibeSoloDelTipo() {
        cliente.setTipoUsuario(admin.getTipoUsuario()); // para que filtre correctamente
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(usuarioService.getAllUsuarios()).thenReturn(List.of(admin, cliente, productor));

        ResponseEntity<List<UsuarioResponseDTO>> response = usuarioController.getUsuariosByTipo("ADMINISTRADOR");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testGetUsuariosActivos_AdminRecibeActivos() {
        admin.setActivo(1);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(usuarioService.getAllUsuarios()).thenReturn(List.of(admin, cliente, productor));

        ResponseEntity<List<UsuarioResponseDTO>> response = usuarioController.getUsuariosActivos();

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetUsuariosInactivos_AdminRecibeInactivos() {
        cliente.setActivo(0);
        productor.setActivo(0);
        admin.setActivo(1);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(usuarioService.getAllUsuarios()).thenReturn(List.of(admin, cliente, productor));

        ResponseEntity<List<UsuarioResponseDTO>> response = usuarioController.getUsuariosInactivos();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testBuscarUsuarios_AdminFiltraPorEmailYActivo() {
        cliente.setActivo(1);
        cliente.setEmail("buscado@dominio.com");
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(usuarioService.getAllUsuarios()).thenReturn(List.of(admin, cliente, productor));

        ResponseEntity<List<UsuarioResponseDTO>> response = usuarioController.buscarUsuarios(null, true, "buscado");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getEmail().contains("buscado"));
    }

    // FUNCIONANDO

        @Test
    void testGetEstadisticasUsuarios_AdminRecibeEstadisticas() {
        admin.setActivo(1);
        cliente.setActivo(1);
        productor.setActivo(0);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(usuarioService.getAllUsuarios()).thenReturn(List.of(admin, cliente, productor));

        ResponseEntity<Object> response = usuarioController.getEstadisticasUsuarios();

        assertEquals(200, response.getStatusCodeValue());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(3, body.get("totalUsuarios"));
        assertEquals(2L, body.get("usuariosActivos"));
        assertEquals(1L, body.get("usuariosInactivos"));
        assertEquals(1L, body.get("productores"));
        assertEquals(1L, body.get("clientes"));
        assertEquals(1L, body.get("administradores"));
    }

    @Test
    void testSyncRolUsuario_AdminSincronizaCorrectamente() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(usuarioService.getUsuarioById(2L)).thenReturn(Optional.of(cliente));

        ResponseEntity<Map<String, Object>> response = usuarioController.syncRolUsuario(2L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Función de sincronización de rol no implementada en este endpoint", response.getBody().get("mensaje"));
        assertNotNull(response.getBody().get("usuario"));
    }

    @Test
    void testSyncRolUsuario_NoEsAdmin_Denegado() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(cliente));

        ResponseEntity<Map<String, Object>> response = usuarioController.syncRolUsuario(2L);

        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    void testGetEstadisticasUsuarios_NoEsAdmin_Denegado() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(cliente));

        ResponseEntity<Object> response = usuarioController.getEstadisticasUsuarios();

        assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    void testSyncRolUsuario_NoExisteUsuario_LanzaExcepcion() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(usuarioService.getUsuarioById(999L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(NotFoundException.class, () -> {
            usuarioController.syncRolUsuario(999L);
        });

        assertTrue(exception.getMessage().contains("Usuario no encontrado con ID"));
    }


    // FUNCIONANDO
    


}
