package com.partymap.backend.controller;

import com.partymap.backend.dto.ReservaDTO;
import com.partymap.backend.model.Evento;
import com.partymap.backend.model.Reserva;
import com.partymap.backend.model.TipoUsuario;
import com.partymap.backend.model.Usuario;
import com.partymap.backend.repository.EventoRepository;
import com.partymap.backend.repository.UsuarioRepository;
import com.partymap.backend.service.ReservaService;
import com.partymap.backend.config.SecurityUtils;
import com.partymap.backend.dto.ReservaResponseDTO;
import com.partymap.backend.exceptions.NotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

    @Mock
    private ReservaService reservaService;
    private Usuario usuarioProductor;


    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ReservaController reservaController;

    private Usuario usuarioAdmin;
    private Usuario usuarioCliente;
    private Evento evento;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        evento = mock(Evento.class);
        lenient().when(evento.getId()).thenReturn(10L);
        lenient().when(evento.getNombre()).thenReturn("Evento de prueba");
        lenient().when(evento.getActivo()).thenReturn(1);
        lenient().when(evento.getPrecioEntrada()).thenReturn(BigDecimal.valueOf(5000));
        lenient().when(evento.isDisponible()).thenReturn(true);
        lenient().when(evento.isEventoPasado()).thenReturn(false);
        lenient().when(evento.getCapacidadMaxima()).thenReturn(100);
        lenient().when(evento.getCuposDisponibles()).thenReturn(50);

        reserva = mock(Reserva.class);
        lenient().when(reserva.getId()).thenReturn(100L);
        lenient().when(reserva.getCantidad()).thenReturn(2);
        lenient().when(reserva.getPrecioUnitario()).thenReturn(BigDecimal.valueOf(5000));
        lenient().when(reserva.getPrecioTotal()).thenReturn(BigDecimal.valueOf(10000));
        lenient().when(reserva.getFechaReserva()).thenReturn(LocalDateTime.now());
        lenient().when(reserva.getUsuario()).thenReturn(usuarioCliente);
        lenient().when(reserva.getEvento()).thenReturn(evento);
        lenient().when(reserva.isActiva()).thenReturn(true);
        lenient().when(reserva.isCancelada()).thenReturn(false);
    }


    @Test
    void testAuth_usuarioNoAutenticado_devuelveUnauthorized() {
        // Arrange
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());
        when(securityUtils.getCurrentUserEmail()).thenReturn(Optional.of("desconocido@test.com"));

        // Act
        ResponseEntity<Map<String, Object>> response = reservaController.testAuth();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("authenticated"));
        assertEquals("Usuario no encontrado en la base de datos", body.get("message"));
        assertEquals("desconocido@test.com", body.get("emailFromJWT"));
    }

    @Test
    void testAuth_usuarioAutenticado_devuelveInfoCorrecta() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(2L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));
        when(securityUtils.getCurrentUserEmail()).thenReturn(Optional.of("cliente@test.com"));

        // Act
        ResponseEntity<Map<String, Object>> response = reservaController.testAuth();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("authenticated"));
        assertEquals("cliente@test.com", body.get("email"));
        assertEquals(usuarioCliente.getId(), body.get("userId"));
        assertEquals("cliente@test.com", body.get("emailFromJWT"));
        assertEquals(TipoUsuario.CLIENTE, body.get("tipoUsuario"));
    }

    // FUNCIONANDO

    @Test
    void getAllReservas_usuarioCliente_devuelveSoloSusReservas() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(2L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));
        when(reservaService.getReservasByUsuarioId(2L)).thenReturn(List.of(reserva));

        // Act
        ResponseEntity<List<ReservaResponseDTO>> response = reservaController.getAllReservas();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    // FUNCIONANDO

    @Test
    void getAllReservas_usuarioAdmin_devuelveTodasLasReservas() {
        // Arrange
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setEmail("admin@test.com");
        usuarioAdmin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioAdmin));
        when(reservaService.getAllreservas()).thenReturn(List.of(reserva));

        // Act
        ResponseEntity<List<ReservaResponseDTO>> response = reservaController.getAllReservas();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getAllReservas_usuarioNoAutenticado_devuelveUnauthorized() {
        // Arrange
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<List<ReservaResponseDTO>> response = reservaController.getAllReservas();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getAllReservas_usuarioSinRolAcceso_devuelveForbidden() {
        // Arrange
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(3L);
        otroUsuario.setEmail("otro@test.com");
        // No seteamos tipoUsuario ni roles booleanos
        // Por defecto isAdministrador(), isCliente(), isProductor() devolverán false

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(otroUsuario));

        // Act
        ResponseEntity<List<ReservaResponseDTO>> response = reservaController.getAllReservas();

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void getReservaById_reservaNoExiste_lanzaNotFoundException() {
        // Arrange
        Long reservaId = 100L;
        when(reservaService.getReservaById(reservaId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(NotFoundException.class, () -> {
            reservaController.getReservaById(reservaId);
        });
    }

    @Test
    void getReservaById_sinPermisoAcceso_devuelveForbidden() {
        // Arrange
        Long reservaId = 100L;

        usuarioCliente = new Usuario();
        usuarioCliente.setId(2L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        when(reserva.getUsuario()).thenReturn(usuarioCliente);
        when(reservaService.getReservaById(reservaId)).thenReturn(Optional.of(reserva));
        when(securityUtils.canAccessReserva(usuarioCliente.getId())).thenReturn(false);

        // Act
        ResponseEntity<ReservaResponseDTO> response = reservaController.getReservaById(reservaId);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getReservaById_conPermisoDevuelveReserva() {
        // Arrange
        Long reservaId = 100L;

        usuarioCliente = new Usuario();
        usuarioCliente.setId(2L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        when(reserva.getUsuario()).thenReturn(usuarioCliente);
        when(securityUtils.canAccessReserva(usuarioCliente.getId())).thenReturn(true);
        when(reservaService.getReservaById(reservaId)).thenReturn(Optional.of(reserva));

        // Act
        ResponseEntity<ReservaResponseDTO> response = reservaController.getReservaById(reservaId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(reservaId, response.getBody().getId());
    }

    // FUNCIONANDO


    @Test
    void createReserva_usuarioNoAutenticado_devuelveUnauthorized() {
        // Arrange
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = reservaController.createReserva(new ReservaDTO());

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void createReserva_usuarioSinPermisoDevuelveForbidden() {
        // Arrange
        Usuario usuarioSinRol = new Usuario();
        usuarioSinRol.setId(99L);
        usuarioSinRol.setEmail("sinrol@test.com");
        // no seteamos tipoUsuario ni roles booleanos

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioSinRol));

        // Act
        ResponseEntity<?> response = reservaController.createReserva(new ReservaDTO());

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void createReserva_dtoNuloDevuelveBadRequest() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(2L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));

        // Act
        ResponseEntity<?> response = reservaController.createReserva(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void createReserva_cantidadNulaDevuelveBadRequest() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(2L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        ReservaDTO dto = new ReservaDTO();
        dto.setEventoId(10L); // valor necesario para que no falle antes

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));

        // Act
        ResponseEntity<?> response = reservaController.createReserva(dto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createReserva_cantidadCeroDevuelveBadRequest() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(2L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        ReservaDTO dto = new ReservaDTO();
        dto.setEventoId(10L);
        dto.setCantidad(0);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));

        // Act
        ResponseEntity<?> response = reservaController.createReserva(dto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createReserva_cantidadMayorA50DevuelveBadRequest() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(2L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        ReservaDTO dto = new ReservaDTO();
        dto.setEventoId(10L);
        dto.setCantidad(51);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));

        // Act
        ResponseEntity<?> response = reservaController.createReserva(dto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createReserva_eventoIdNuloDevuelveBadRequest() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(2L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        ReservaDTO dto = new ReservaDTO();
        dto.setCantidad(2);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));

        // Act
        ResponseEntity<?> response = reservaController.createReserva(dto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void createReserva_eventoNoEncontradoDevuelveBadRequest() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(2L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        ReservaDTO dto = new ReservaDTO();
        dto.setCantidad(2);
        dto.setEventoId(999L);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));
        when(eventoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = reservaController.createReserva(dto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createReserva_eventoInactivoDevuelveBadRequest() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(2L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        Evento eventoMock = mock(Evento.class);
        when(eventoMock.getActivo()).thenReturn(0);

        ReservaDTO dto = new ReservaDTO();
        dto.setCantidad(2);
        dto.setEventoId(10L);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(eventoMock));

        // Act
        ResponseEntity<?> response = reservaController.createReserva(dto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createReserva_eventoPasadoDevuelveBadRequest() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(2L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        Evento eventoMock = mock(Evento.class);
        when(eventoMock.getActivo()).thenReturn(1);
        when(eventoMock.isEventoPasado()).thenReturn(true);

        ReservaDTO dto = new ReservaDTO();
        dto.setCantidad(2);
        dto.setEventoId(10L);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(eventoMock));

        // Act
        ResponseEntity<?> response = reservaController.createReserva(dto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createReserva_eventoNoDisponibleDevuelveBadRequest() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(2L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        Evento eventoMock = mock(Evento.class);
        when(eventoMock.getActivo()).thenReturn(1);
        when(eventoMock.isEventoPasado()).thenReturn(false);
        when(eventoMock.isDisponible()).thenReturn(false);

        ReservaDTO dto = new ReservaDTO();
        dto.setCantidad(2);
        dto.setEventoId(10L);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(eventoMock));

        // Act
        ResponseEntity<?> response = reservaController.createReserva(dto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // FUNCIONANDO


    @Test
    void createReserva_usuarioYaTieneReservaActivaDevuelveBadRequest() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(2L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        Evento eventoMock = mock(Evento.class);
        when(eventoMock.getId()).thenReturn(10L); // Clave
        when(eventoMock.getActivo()).thenReturn(1);
        when(eventoMock.isEventoPasado()).thenReturn(false);
        when(eventoMock.isDisponible()).thenReturn(true);
        when(eventoMock.getCuposDisponibles()).thenReturn(10);

        Reserva reservaActiva = mock(Reserva.class);
        when(reservaActiva.isActiva()).thenReturn(true);
        when(reservaActiva.getEvento()).thenReturn(eventoMock);

        ReservaDTO dto = new ReservaDTO();
        dto.setCantidad(2);
        dto.setEventoId(10L);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(eventoMock));
        when(reservaService.getReservasByUsuarioId(usuarioCliente.getId()))
                .thenReturn(List.of(reservaActiva));

        // Act
        ResponseEntity<?> response = reservaController.createReserva(dto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void createReserva_cuposInsuficientesDevuelveBadRequest() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(2L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        Evento eventoMock = mock(Evento.class);
        when(eventoMock.getActivo()).thenReturn(1);
        when(eventoMock.isEventoPasado()).thenReturn(false);
        when(eventoMock.isDisponible()).thenReturn(true);
        when(eventoMock.getCuposDisponibles()).thenReturn(1); // solo 1 disponible

        ReservaDTO dto = new ReservaDTO();
        dto.setCantidad(5); // quiere 5
        dto.setEventoId(10L);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(eventoMock));

        // Act
        ResponseEntity<?> response = reservaController.createReserva(dto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void getReservasCanceladas_adminDevuelveListaVacia() {
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setEmail("admin@test.com");
        usuarioAdmin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioAdmin));
        when(reservaService.getReservasCanceladas()).thenReturn(List.of());

        ResponseEntity<?> response = reservaController.getReservasCanceladas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((List<?>) response.getBody()).isEmpty());
    }

    // FUNCIONANDO


    @Test
    void isReservaActiva_usuarioNoTienePermisoDevuelveForbidden() {
        usuarioCliente = new Usuario();
        usuarioCliente.setId(1L);
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(99L);

        Reserva reserva = new Reserva();
        reserva.setId(10L);
        reserva.setUsuario(otroUsuario);

        when(reservaService.getReservaById(10L)).thenReturn(Optional.of(reserva));
        when(securityUtils.canAccessReserva(99L)).thenReturn(false);

        ResponseEntity<?> response = reservaController.isReservaActiva(10L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void getReservasActivas_productorDevuelveListaVacia() {
        usuarioProductor = new Usuario();
        usuarioProductor.setId(2L);
        usuarioProductor.setEmail("prod@test.com");
        usuarioProductor.setTipoUsuario(TipoUsuario.PRODUCTOR);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioProductor));
        when(reservaService.getReservasActivas()).thenReturn(List.of());

        ResponseEntity<?> response = reservaController.getReservasActivas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((List<?>) response.getBody()).isEmpty());
    }

    // FUNCIONANDO

    @Test
    void reactivarReserva_sinUsuarioAutenticadoDevuelveUnauthorized() {
        // Arrange
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = reservaController.reactivarReserva(1L);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void getPrecioTotal_sinPermisoDevuelveForbidden() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(4L);
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(5L);

        Reserva reserva = new Reserva();
        reserva.setUsuario(otroUsuario);

        when(securityUtils.canAccessReserva(5L)).thenReturn(false);
        when(reservaService.getReservaById(1L)).thenReturn(Optional.of(reserva));

        // Act
        ResponseEntity<?> response = reservaController.getPrecioTotal(1L);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void isReservaActiva_reservaNoEncontradaDevuelveNotFound() {
        // Arrange
        when(reservaService.getReservaById(99L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = reservaController.isReservaActiva(99L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void updateReserva_reservaNoEncontradaDevuelveNotFound() {
        // Arrange
        when(reservaService.getReservaById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = reservaController.updateReserva(1L, new ReservaDTO());

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void updateReserva_sinPermisosDevuelveForbidden() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(10L);
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(20L);

        Reserva reservaExistente = new Reserva();
        reservaExistente.setUsuario(otroUsuario);

        when(reservaService.getReservaById(1L)).thenReturn(Optional.of(reservaExistente));
        when(securityUtils.canModifyReserva(20L)).thenReturn(false);

        // Act
        ResponseEntity<?> response = reservaController.updateReserva(1L, new ReservaDTO());

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void deleteReserva_sinUsuarioAutenticadoDevuelveUnauthorized() {
        // Arrange
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = reservaController.deleteReserva(1L);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void deleteReserva_usuarioNoEsAdminDevuelveForbidden() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(99L);
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));

        // Act
        ResponseEntity<?> response = reservaController.deleteReserva(1L);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void getReservasByUsuario_clienteAccedeAJenoDevuelveForbidden() {
        // Arrange
        usuarioCliente = new Usuario();
        usuarioCliente.setId(5L);
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));

        // Act
        ResponseEntity<?> response = reservaController.getReservasByUsuario(9L);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void cancelarReserva_reservaNoExiste_devuelveNotFound() {
        // Arrange
        when(reservaService.getReservaById(1L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = reservaController.cancelarReserva(1L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void cancelarReserva_sinPermisos_devuelveForbidden() {
        // Arrange
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(99L);

        Reserva reserva = new Reserva();
        reserva.setUsuario(otroUsuario);

        when(reservaService.getReservaById(1L)).thenReturn(Optional.of(reserva));
        when(securityUtils.canModifyReserva(99L)).thenReturn(false);

        // Act
        ResponseEntity<?> response = reservaController.cancelarReserva(1L);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void cancelarReserva_lanzaIllegalArgumentException_devuelveBadRequest() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(10L);

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);

        when(reservaService.getReservaById(1L)).thenReturn(Optional.of(reserva));
        when(securityUtils.canModifyReserva(10L)).thenReturn(true);
        when(reservaService.cancelarReserva(1L)).thenThrow(new IllegalArgumentException("Error"));

        // Act
        ResponseEntity<?> response = reservaController.cancelarReserva(1L);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void cancelarReserva_lanzaNotFoundException_devuelveNotFound() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(10L);

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);

        when(reservaService.getReservaById(1L)).thenReturn(Optional.of(reserva));
        when(securityUtils.canModifyReserva(10L)).thenReturn(true);
        when(reservaService.cancelarReserva(1L)).thenThrow(new NotFoundException("No encontrada"));

        // Act
        ResponseEntity<?> response = reservaController.cancelarReserva(1L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void getReservasPorRangoFechas_usuarioNoAutenticado_devuelveUnauthorized() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        ResponseEntity<?> response = reservaController.getReservasPorRangoFechas(
                "2024-01-01T00:00", "2024-12-31T23:59");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getReservasPorRangoFechas_usuarioCliente_devuelveForbidden() {
        Usuario usuarioCliente = new Usuario();
        usuarioCliente.setId(10L);
        usuarioCliente.setEmail("cliente@test.com");
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));

        ResponseEntity<?> response = reservaController.getReservasPorRangoFechas(
                "2024-01-01T00:00", "2024-12-31T23:59");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }



    @Test
    void getReservasPorRangoFechas_formatoInvalido_devuelveBadRequest() {
        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setEmail("admin@test.com");
        usuarioAdmin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioAdmin));

        ResponseEntity<?> response = reservaController.getReservasPorRangoFechas(
                "fecha-mala", "2024-12-31T23:59");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void getReservasPorPrecioMinimo_sinUsuarioAutenticado_devuelveUnauthorized() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        ResponseEntity<?> response = reservaController.getReservasPorPrecioMinimo(BigDecimal.valueOf(10000));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getReservasPorPrecioMinimo_usuarioCliente_devuelveForbidden() {
        usuarioCliente = new Usuario();
        usuarioCliente.setId(5L);
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));

        ResponseEntity<?> response = reservaController.getReservasPorPrecioMinimo(BigDecimal.valueOf(10000));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getReservasPorPrecioMinimo_adminDevuelveListaOk() {
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioAdmin));
        when(reservaService.getReservasPorPrecioMinimo(any())).thenReturn(List.of());

        ResponseEntity<?> response = reservaController.getReservasPorPrecioMinimo(BigDecimal.valueOf(10000));

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void getReservasPorPrecioMaximo_sinUsuarioAutenticado_devuelveUnauthorized() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        ResponseEntity<?> response = reservaController.getReservasPorPrecioMaximo(BigDecimal.valueOf(5000));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getReservasPorPrecioMaximo_usuarioCliente_devuelveForbidden() {
        usuarioCliente = new Usuario();
        usuarioCliente.setId(10L);
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));

        ResponseEntity<?> response = reservaController.getReservasPorPrecioMaximo(BigDecimal.valueOf(5000));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getReservasPorPrecioMaximo_adminDevuelveListaOk() {
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioAdmin));
        when(reservaService.getReservasPorPrecioMaximo(any())).thenReturn(List.of());

        ResponseEntity<?> response = reservaController.getReservasPorPrecioMaximo(BigDecimal.valueOf(5000));

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void getReservasPorCantidad_sinUsuarioAutenticado_devuelveUnauthorized() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        ResponseEntity<?> response = reservaController.getReservasPorCantidad(3);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getReservasPorCantidad_usuarioCliente_devuelveForbidden() {
        usuarioCliente = new Usuario();
        usuarioCliente.setId(8L);
        usuarioCliente.setTipoUsuario(TipoUsuario.CLIENTE);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioCliente));

        ResponseEntity<?> response = reservaController.getReservasPorCantidad(3);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getReservasPorCantidad_adminDevuelveListaOk() {
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioAdmin));
        when(reservaService.getReservasPorCantidad(anyInt())).thenReturn(List.of());

        ResponseEntity<?> response = reservaController.getReservasPorCantidad(3);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // FUNCIONANDO

    
















}
