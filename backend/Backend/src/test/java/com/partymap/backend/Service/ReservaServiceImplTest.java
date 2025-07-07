package com.partymap.backend.service;

import com.partymap.backend.model.Reserva;
import com.partymap.backend.repository.ReservaRepository;
import com.partymap.backend.service.Impl.ReservaServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReservaServiceImplTest {

    private ReservaRepository reservaRepository;
    private ReservaServiceImpl reservaService;

    @BeforeEach
    void setUp() {
        reservaRepository = mock(ReservaRepository.class);
        reservaService = new ReservaServiceImpl(reservaRepository);
    }

    @Test
    void deberiaRetornarListaReservasVacia() {
        when(reservaRepository.findAll()).thenReturn(Collections.emptyList());

        List<Reserva> reservas = reservaService.getAllreservas();

        assertNotNull(reservas);
        assertTrue(reservas.isEmpty());
        verify(reservaRepository, times(1)).findAll();
    }
}