package com.partymap.backend.service;

import com.partymap.backend.dto.ReservaResponseDTO;
import com.partymap.backend.model.EstadoReserva;
import com.partymap.backend.model.Evento;
import com.partymap.backend.model.Reserva;
import com.partymap.backend.model.Usuario;
import com.partymap.backend.repository.EventoRepository;
import com.partymap.backend.repository.ReservaRepository;
import com.partymap.backend.repository.UsuarioRepository;
import com.partymap.backend.service.Impl.ReservaServiceImpl;
import com.partymap.backend.exceptions.NotFoundException;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;


@ExtendWith(MockitoExtension.class)
public class ReservaServiceImplTest {

    @InjectMocks
    private ReservaServiceImpl reservaService;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;


    


    @Test
    void testGetAllReservas() {
        List<Reserva> reservas = List.of(new Reserva(), new Reserva());
        when(reservaRepository.findAll()).thenReturn(reservas);

        List<Reserva> result = reservaService.getAllreservas();

        assertEquals(2, result.size());
        verify(reservaRepository).findAll();
    }

    @Test
    void testGetReservaById_found() {
        Reserva reserva = new Reserva();
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        Optional<Reserva> result = reservaService.getReservaById(1L);

        assertTrue(result.isPresent());
        verify(reservaRepository).findById(1L);
    }

    @Test
    void testGetReservaById_notFound() {
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Reserva> result = reservaService.getReservaById(999L);

        assertFalse(result.isPresent());
    }


    @Test
    void testGetReservasActivas() {
        Reserva activa = new Reserva();
        activa.setEstado(EstadoReserva.RESERVADA);

        when(reservaRepository.findAll()).thenReturn(List.of(activa));

        List<Reserva> result = reservaService.getReservasActivas();

        assertEquals(1, result.size());
    }

    @Test
    void testGetReservasCanceladas() {
        Reserva cancelada = new Reserva();
        cancelada.setEstado(EstadoReserva.CANCELADA);

        when(reservaRepository.findAll()).thenReturn(List.of(cancelada));

        List<Reserva> result = reservaService.getReservasCanceladas();

        assertEquals(1, result.size());
    }

    @Test
    void testCancelarReserva_found() {
        Reserva reserva = new Reserva();
        reserva.setEstado(EstadoReserva.RESERVADA);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);

        Reserva result = reservaService.cancelarReserva(1L);

        assertEquals(EstadoReserva.CANCELADA, result.getEstado());
    }

    @Test
    void testCancelarReserva_alreadyCancelled() {
        Reserva reserva = new Reserva();
        reserva.setEstado(EstadoReserva.CANCELADA);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        assertThrows(IllegalArgumentException.class, () -> reservaService.cancelarReserva(1L));
    }

    @Test
    void testGetPrecioTotalReserva_found() {
        Reserva reserva = new Reserva();
        reserva.setPrecioTotal(new BigDecimal("20000"));
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        BigDecimal total = reservaService.getPrecioTotalReserva(1L);

        assertEquals(new BigDecimal("20000"), total);
    }

    @Test
    void testGetPrecioTotalReserva_zero() {
        Reserva reserva = new Reserva();
        reserva.setPrecioTotal(BigDecimal.ZERO);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        BigDecimal total = reservaService.getPrecioTotalReserva(1L);

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void testCancelarReserva_notFound() {
        when(reservaRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reservaService.cancelarReserva(2L));
        verify(reservaRepository).findById(2L);
    }

    @Test
    void testGetPrecioTotalReserva_notFound() {
        when(reservaRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reservaService.getPrecioTotalReserva(3L));
        verify(reservaRepository).findById(3L);
    }

    @Test
    void testGetAllReservas_emptyList() {
        when(reservaRepository.findAll()).thenReturn(Collections.emptyList());

        List<Reserva> result = reservaService.getAllreservas();

        assertTrue(result.isEmpty());
        verify(reservaRepository).findAll();
    }

    @Test
    void testCreateReserva_success() throws Exception {
        Reserva reserva = new Reserva();
        reserva.setCantidad(2);
        reserva.setUsuario(new Usuario());
        Evento evento = new Evento();
        evento.setFecha(java.time.LocalDateTime.now().plusDays(1)); // Evento futuro
        reserva.setEvento(evento);
        when(reservaRepository.save(any())).thenReturn(reserva);

        Reserva result = reservaService.createReserva(reserva);

        assertNotNull(result);
        verify(reservaRepository).save(reserva);
    }

    @Test
    void testCreateReserva_exception() {
        Reserva reserva = new Reserva();
        // No seteamos usuario o evento para forzar la excepción antes de llamar a save

        assertThrows(Exception.class, () -> reservaService.createReserva(reserva));
    }

    @Test
    void testUpdateReserva_found() throws Exception {
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setCantidad(3);
        reserva.setUsuario(new Usuario());
        Evento evento = new Evento();
        evento.setFecha(java.time.LocalDateTime.now().plusDays(1)); // Evento futuro
        reserva.setEvento(evento);

        when(reservaRepository.existsById(1L)).thenReturn(true);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);

        Reserva result = reservaService.updateReserva(1L, reserva);

        assertNotNull(result);
        verify(reservaRepository).existsById(1L);
        verify(reservaRepository).findById(1L);
        verify(reservaRepository).save(reserva);
    }

    @Test
    void testUpdateReserva_updateFields() throws Exception {
        Reserva original = new Reserva();
        original.setId(1L);
        original.setCantidad(1);
        original.setUsuario(new Usuario());
        Evento evento = new Evento();
        evento.setFecha(java.time.LocalDateTime.now().plusDays(1));
        original.setEvento(evento);

        Reserva updated = new Reserva();
        updated.setId(1L);
        updated.setCantidad(5);
        updated.setUsuario(new Usuario());
        updated.setEvento(evento);

        when(reservaRepository.existsById(1L)).thenReturn(true);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(original));
        when(reservaRepository.save(any())).thenReturn(updated);

        Reserva result = reservaService.updateReserva(1L, updated);

        assertEquals(5, result.getCantidad());
    }

    @Test
    void testUpdateReserva_notFound() {
        Reserva reserva = new Reserva();
        reserva.setId(2L);
        reserva.setUsuario(new Usuario());
        reserva.setEvento(new Evento());

        when(reservaRepository.existsById(2L)).thenReturn(false);

        assertThrows(com.partymap.backend.exceptions.NotFoundException.class, () -> reservaService.updateReserva(2L, reserva));
        verify(reservaRepository).existsById(2L);
    }


    @Test
    void testGetAllReservas_onlyActive() {
        Reserva activa = new Reserva();
        activa.setActivo(1);
        Reserva inactiva = new Reserva();
        inactiva.setActivo(0);

        when(reservaRepository.findAll()).thenReturn(List.of(activa, inactiva));

        List<Reserva> result = reservaService.getAllreservas();

        assertEquals(1, result.size());
        assertTrue(result.contains(activa));
        assertFalse(result.contains(inactiva));
    }


    @Test
    void testGetReservasActivas_withMixedEstados() {
        Reserva activa = new Reserva();
        activa.setEstado(EstadoReserva.RESERVADA);
        Reserva cancelada = new Reserva();
        cancelada.setEstado(EstadoReserva.CANCELADA);

        when(reservaRepository.findAll()).thenReturn(List.of(activa, cancelada));

        List<Reserva> result = reservaService.getReservasActivas();

        assertEquals(1, result.size());
        assertEquals(EstadoReserva.RESERVADA, result.get(0).getEstado());
    }

    @Test
    void testGetReservasCanceladas_withMixedEstados() {
        Reserva activa = new Reserva();
        activa.setEstado(EstadoReserva.RESERVADA);
        Reserva cancelada = new Reserva();
        cancelada.setEstado(EstadoReserva.CANCELADA);

        when(reservaRepository.findAll()).thenReturn(List.of(activa, cancelada));

        List<Reserva> result = reservaService.getReservasCanceladas();

        assertEquals(1, result.size());
        assertEquals(EstadoReserva.CANCELADA, result.get(0).getEstado());
    }

    @Test
    void testGetReservaById_nullId() {
        when(reservaRepository.findById(null)).thenReturn(Optional.empty());

        Optional<Reserva> result = reservaService.getReservaById(null);

        assertFalse(result.isPresent());
    }



    @Test
    void testCreateReserva_eventoPasado() {
        Reserva reserva = new Reserva();
        reserva.setCantidad(1);
        reserva.setUsuario(new Usuario());
        Evento evento = new Evento();
        evento.setFecha(java.time.LocalDateTime.now().minusDays(1)); // Evento pasado
        reserva.setEvento(evento);

        Exception ex = assertThrows(Exception.class, () -> reservaService.createReserva(reserva));
        assertTrue(ex.getMessage().toLowerCase().contains("pasado"));
    }

    

    @Test
    void testCreateReserva_sinCantidad() {
        Reserva reserva = new Reserva();
        reserva.setUsuario(new Usuario());
        Evento evento = new Evento();
        evento.setFecha(java.time.LocalDateTime.now().plusDays(1));
        reserva.setEvento(evento);

        Exception ex = assertThrows(Exception.class, () -> reservaService.createReserva(reserva));
        assertTrue(ex.getMessage().toLowerCase().contains("cantidad"));
    }


    @Test
    void testCreateReserva_sinEvento() {
        Reserva reserva = new Reserva();
        reserva.setCantidad(1);
        reserva.setUsuario(new Usuario());

        Exception ex = assertThrows(Exception.class, () -> reservaService.createReserva(reserva));
        assertTrue(ex.getMessage().toLowerCase().contains("evento"));
    }

    @Test
    void testCreateReserva_sinUsuario() {
        Reserva reserva = new Reserva();
        reserva.setCantidad(1);
        Evento evento = new Evento();
        evento.setFecha(java.time.LocalDateTime.now().plusDays(1));
        reserva.setEvento(evento);

        Exception ex = assertThrows(Exception.class, () -> reservaService.createReserva(reserva));
        assertTrue(ex.getMessage().toLowerCase().contains("usuario"));
    }

    @Test
    void testGetPrecioTotalReserva_negative() {
        Reserva reserva = new Reserva();
        reserva.setPrecioTotal(new BigDecimal("-1000"));
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));

        BigDecimal total = reservaService.getPrecioTotalReserva(10L);

        assertEquals(new BigDecimal("-1000"), total);
    }

    @Test
    void testGetReservasActivas_emptyList() {
        when(reservaRepository.findAll()).thenReturn(Collections.emptyList());

        List<Reserva> result = reservaService.getReservasActivas();

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetReservasCanceladas_emptyList() {
        when(reservaRepository.findAll()).thenReturn(Collections.emptyList());

        List<Reserva> result = reservaService.getReservasCanceladas();

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllReservas_oneActive() {
        Reserva activa = new Reserva();
        activa.setActivo(1);

        when(reservaRepository.findAll()).thenReturn(List.of(activa));

        List<Reserva> result = reservaService.getAllreservas();

        assertEquals(1, result.size());
        assertTrue(result.contains(activa));
    }

    @Test
    void testGetAllReservas_allInactive() {
        Reserva inactiva1 = new Reserva();
        inactiva1.setActivo(0);
        Reserva inactiva2 = new Reserva();
        inactiva2.setActivo(0);

        when(reservaRepository.findAll()).thenReturn(List.of(inactiva1, inactiva2));

        List<Reserva> result = reservaService.getAllreservas();

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetReservaById_negativeId() {
        when(reservaRepository.findById(-1L)).thenReturn(Optional.empty());

        Optional<Reserva> result = reservaService.getReservaById(-1L);

        assertFalse(result.isPresent());
    }

    @Test
    void testCancelarReserva_activa_verificaSave() {
        Reserva reserva = new Reserva();
        reserva.setEstado(EstadoReserva.RESERVADA);
        when(reservaRepository.findById(5L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any())).thenReturn(reserva);

        Reserva result = reservaService.cancelarReserva(5L);

        assertEquals(EstadoReserva.CANCELADA, result.getEstado());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void testCancelarReserva_inactiva_noVerificaSave() {
        Reserva reserva = new Reserva();
        reserva.setEstado(EstadoReserva.CANCELADA);
        when(reservaRepository.findById(6L)).thenReturn(Optional.of(reserva));

        assertThrows(IllegalArgumentException.class, () -> reservaService.cancelarReserva(6L));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void testUpdateReserva_sameCantidad() throws Exception {
        Reserva original = new Reserva();
        original.setId(1L);
        original.setCantidad(2);
        original.setUsuario(new Usuario());
        Evento evento = new Evento();
        evento.setFecha(java.time.LocalDateTime.now().plusDays(1));
        original.setEvento(evento);

        Reserva updated = new Reserva();
        updated.setId(1L);
        updated.setCantidad(2);
        updated.setUsuario(new Usuario());
        updated.setEvento(evento);

        when(reservaRepository.existsById(1L)).thenReturn(true);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(original));
        when(reservaRepository.save(any())).thenReturn(updated);

        Reserva result = reservaService.updateReserva(1L, updated);

        assertEquals(2, result.getCantidad());
    }

    @Test
    void testGetAllReservas_multipleCalls() {
        when(reservaRepository.findAll()).thenReturn(Collections.emptyList());

        reservaService.getAllreservas();
        reservaService.getAllreservas();
        reservaService.getAllreservas();

        verify(reservaRepository, times(3)).findAll();
    }

    @Test
    void testGetReservaById_largeId() {
        when(reservaRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        Optional<Reserva> result = reservaService.getReservaById(Long.MAX_VALUE);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetPrecioTotalReserva_largeValue() {
        Reserva reserva = new Reserva();
        reserva.setPrecioTotal(new BigDecimal("999999999999"));
        when(reservaRepository.findById(123L)).thenReturn(Optional.of(reserva));

        BigDecimal total = reservaService.getPrecioTotalReserva(123L);

        assertEquals(new BigDecimal("999999999999"), total);
    }

    @Test
    void testGetReservasActivas_allCanceladas() {
        Reserva cancelada1 = new Reserva();
        cancelada1.setEstado(EstadoReserva.CANCELADA);
        Reserva cancelada2 = new Reserva();
        cancelada2.setEstado(EstadoReserva.CANCELADA);

        when(reservaRepository.findAll()).thenReturn(List.of(cancelada1, cancelada2));

        List<Reserva> result = reservaService.getReservasActivas();

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetReservasCanceladas_allActivas() {
        Reserva activa1 = new Reserva();
        activa1.setEstado(EstadoReserva.RESERVADA);
        Reserva activa2 = new Reserva();
        activa2.setEstado(EstadoReserva.RESERVADA);

        when(reservaRepository.findAll()).thenReturn(List.of(activa1, activa2));

        List<Reserva> result = reservaService.getReservasCanceladas();

        assertTrue(result.isEmpty());
    }

    @Test
    void testCancelarReserva_largeId() {
        when(reservaRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reservaService.cancelarReserva(Long.MAX_VALUE));
    }

    @Test
    void testGetReservasPorRangoFechas_empty() {
        when(reservaRepository.findAll()).thenReturn(Collections.emptyList());
        List<Reserva> result = reservaService.getReservasPorRangoFechas(LocalDateTime.now().minusDays(1), LocalDateTime.now());
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetReservasPorRangoFechas_found() {
        Reserva reserva = new Reserva();
        reserva.setActivo(1);
        reserva.setFechaReserva(LocalDateTime.now().minusHours(1));
        when(reservaRepository.findAll()).thenReturn(List.of(reserva));
        List<Reserva> result = reservaService.getReservasPorRangoFechas(LocalDateTime.now().minusDays(1), LocalDateTime.now());
        assertEquals(1, result.size());
    }

    @Test
    void testGetReservasUsuarioPorRangoFechas_found() {
        Reserva reserva = new Reserva();
        reserva.setActivo(1);
        reserva.setFechaReserva(LocalDateTime.now().minusHours(1));
        reserva.setUsuario(new Usuario());
        reserva.getUsuario().setId(99L);
        when(reservaRepository.findAll()).thenReturn(List.of(reserva));
        List<Reserva> result = reservaService.getReservasUsuarioPorRangoFechas(99L, LocalDateTime.now().minusDays(1), LocalDateTime.now());
        assertEquals(1, result.size());
    }

    @Test
    void testGetReservasEventoPorRangoFechas_found() {
        Reserva reserva = new Reserva();
        reserva.setActivo(1);
        reserva.setFechaReserva(LocalDateTime.now().minusHours(1));
        Evento evento = new Evento();
        evento.setId(77L);
        reserva.setEvento(evento);
        when(reservaRepository.findAll()).thenReturn(List.of(reserva));
        List<Reserva> result = reservaService.getReservasEventoPorRangoFechas(77L, LocalDateTime.now().minusDays(1), LocalDateTime.now());
        assertEquals(1, result.size());
    }

    @Test
    void testGetReservasPorPrecioMinimo_found() {
        Reserva reserva = new Reserva();
        reserva.setActivo(1);
        reserva.setPrecioTotal(new BigDecimal("2000"));
        when(reservaRepository.findAll()).thenReturn(List.of(reserva));
        List<Reserva> result = reservaService.getReservasPorPrecioMinimo(new BigDecimal("1000"));
        assertEquals(1, result.size());
    }

    @Test
    void testGetReservasPorPrecioMaximo_found() {
        Reserva reserva = new Reserva();
        reserva.setActivo(1);
        reserva.setPrecioTotal(new BigDecimal("500"));
        when(reservaRepository.findAll()).thenReturn(List.of(reserva));
        List<Reserva> result = reservaService.getReservasPorPrecioMaximo(new BigDecimal("1000"));
        assertEquals(1, result.size());
    }

    @Test
    void testGetReservasPorCantidad_found() {
        Reserva reserva = new Reserva();
        reserva.setActivo(1);
        reserva.setCantidad(3);
        when(reservaRepository.findAll()).thenReturn(List.of(reserva));
        List<Reserva> result = reservaService.getReservasPorCantidad(3);
        assertEquals(1, result.size());
    }

    @Test
    void testGetReservasPorCantidadMinima_found() {
        Reserva reserva = new Reserva();
        reserva.setActivo(1);
        reserva.setCantidad(10);
        when(reservaRepository.findAll()).thenReturn(List.of(reserva));
        List<Reserva> result = reservaService.getReservasPorCantidadMinima(5);
        assertEquals(1, result.size());
    }

    @Test
    void testIsReservaActiva_true() {
        Reserva reserva = new Reserva();
        Reserva spyReserva = spy(reserva);
        doReturn(true).when(spyReserva).isActiva();
        when(reservaRepository.findById(2L)).thenReturn(Optional.of(spyReserva));
        assertTrue(reservaService.isReservaActiva(2L));
    }

    @Test
    void testIsReservaActiva_false() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.empty());
        assertFalse(reservaService.isReservaActiva(1L));
    }

    @Test
    void testIsReservaCancelada_true() {
        Reserva reserva = new Reserva();
        Reserva spyReserva = spy(reserva);
        doReturn(true).when(spyReserva).isCancelada();
        when(reservaRepository.findById(3L)).thenReturn(Optional.of(spyReserva));
        assertTrue(reservaService.isReservaCancelada(3L));
    }

    @Test
    void testIsReservaCancelada_false() {
        when(reservaRepository.findById(4L)).thenReturn(Optional.empty());
        assertFalse(reservaService.isReservaCancelada(4L));
    }

    @Test
    void testGetEstadisticasCompletas_empty() {
        when(reservaRepository.findAll()).thenReturn(Collections.emptyList());
        Object estadisticas = reservaService.getEstadisticasCompletas();
        assertNotNull(estadisticas);
        assertTrue(estadisticas instanceof Map);
    }
   

   @Test
    void testGetPrecioTotalReservaFound() {
        Long reservaId = 1L;
        Reserva reserva = new Reserva();
        reserva.setPrecioTotal(new BigDecimal("123.45"));
        
        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
        
        BigDecimal resultado = reservaService.getPrecioTotalReserva(reservaId);
        
        assertEquals(new BigDecimal("123.45"), resultado);
    }

    @Test
    void testGetPrecioTotalReservaNotFound() {
        Long reservaId = 999L;
        when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            reservaService.getPrecioTotalReserva(reservaId);
        });

        assertTrue(exception.getMessage().contains("Reserva no encontrada"));
    }

        

    @Test
    void testReactivarReservaSuccess() {
        Long reservaId = 1L;
        int cantidad = 2;

        // Crear mock del evento y simular comportamiento
        Evento eventoMock = mock(Evento.class);
        when(eventoMock.isDisponible()).thenReturn(true);
        when(eventoMock.getCuposDisponibles()).thenReturn(10);

        // Crear reserva real y configurar comportamiento
        Reserva reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setCantidad(cantidad);
        reserva.setEvento(eventoMock);

        // Mock repositorio
        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar lógica
        Reserva result = reservaService.reactivarReserva(reservaId);

        // Validaciones
        assertNotNull(result);
        assertEquals(EstadoReserva.RESERVADA, result.getEstado());

        verify(reservaRepository).findById(reservaId);
        verify(reservaRepository).save(reserva);
    }

        

    @Test
    void testGetReservasByEventoId_CasoExitoso() {
        // Arrange
        Long eventoId = 100L;

        Evento evento1 = new Evento();
        evento1.setId(eventoId);

        Evento evento2 = new Evento();
        evento2.setId(200L); // otro evento

        Reserva reservaActivaEvento1 = new Reserva();
        reservaActivaEvento1.setActivo(1);
        reservaActivaEvento1.setEvento(evento1);

        Reserva reservaInactivaEvento1 = new Reserva();
        reservaInactivaEvento1.setActivo(0);
        reservaInactivaEvento1.setEvento(evento1);

        Reserva reservaActivaEvento2 = new Reserva();
        reservaActivaEvento2.setActivo(1);
        reservaActivaEvento2.setEvento(evento2);

        List<Reserva> todasLasReservas = Arrays.asList(
                reservaActivaEvento1,
                reservaInactivaEvento1,
                reservaActivaEvento2
        );

        when(reservaRepository.findAll()).thenReturn(todasLasReservas);

        // Act
        List<Reserva> resultado = reservaService.getReservasByEventoId(eventoId);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(eventoId, resultado.get(0).getEvento().getId());
        assertEquals(1, resultado.get(0).getActivo());

        verify(reservaRepository, times(1)).findAll();
    }


    @Test
    void testDeleteReserva_CasoExitoso() throws IOException {
        // Arrange
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setActivo(1);

        when(reservaRepository.existsById(1L)).thenReturn(true);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        // Act
        reservaService.deleteReserva(reserva);

        // Assert
        assertEquals(0, reserva.getActivo()); // debe estar desactivada
        verify(reservaRepository).save(reserva);
    }

    @Test
    void testDeleteReserva_ReservaNoEncontrada() {
        // Arrange
        Reserva reserva = new Reserva();
        reserva.setId(99L);

        when(reservaRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> reservaService.deleteReserva(reserva)
        );

        assertEquals("Reserva no encontrada con ID: 99", exception.getMessage());
        verify(reservaRepository, never()).save(any());
    }


    @Test
    void testReactivarReserva_EventoNoDisponible_LanzaExcepcion() {
        Evento evento = mock(Evento.class);
        when(evento.isDisponible()).thenReturn(false);

        Reserva reserva = mock(Reserva.class);
        when(reserva.isCancelada()).thenReturn(true);
        when(reserva.getEvento()).thenReturn(evento);
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.reactivarReserva(1L);
        });

        assertEquals("No se puede reactivar la reserva porque el evento no está disponible", exception.getMessage());
    }

        

    @Test
    void testReactivarReserva_ReservaNoEncontrada_LanzaNotFoundException() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            reservaService.reactivarReserva(99L);
        });

        assertEquals("Reserva no encontrada con ID: 99", exception.getMessage());
    }

    

    @Test
    void testReactivarReserva_CasoExitoso() {
        // Arrange
        Long reservaId = 1L;

        Reserva reservaMock = mock(Reserva.class);
        Evento eventoMock = mock(Evento.class);

        when(reservaMock.isCancelada()).thenReturn(true);
        when(reservaMock.getEvento()).thenReturn(eventoMock);
        when(reservaMock.getCantidad()).thenReturn(2);

        when(eventoMock.isDisponible()).thenReturn(true);
        when(eventoMock.getCuposDisponibles()).thenReturn(5); // ← Aquí el cambio: Integer en lugar de long

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reservaMock));
        when(reservaRepository.save(reservaMock)).thenReturn(reservaMock);

        // Act
        Reserva resultado = reservaService.reactivarReserva(reservaId);

        // Assert
        assertNotNull(resultado);
        verify(reservaMock).setEstado(EstadoReserva.RESERVADA);
        verify(reservaRepository).save(reservaMock);
    }






}
