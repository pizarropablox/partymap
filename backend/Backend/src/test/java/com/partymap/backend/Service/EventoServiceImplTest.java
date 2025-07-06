package com.partymap.backend.Service;

import com.partymap.backend.Model.Evento;
import com.partymap.backend.Repository.EventoRepository;
import com.partymap.backend.Repository.UbicacionRepository;
import com.partymap.backend.Service.Impl.EventoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EventoServiceImplTest {

    private EventoRepository eventoRepository;
    private UbicacionRepository ubicacionRepository;
    private EventoServiceImpl eventoService;

    @BeforeEach
    void setUp() {
        eventoRepository = mock(EventoRepository.class);
        ubicacionRepository = mock(UbicacionRepository.class);
        eventoService = new EventoServiceImpl(eventoRepository, ubicacionRepository);
    }

    @Test
    void deberiaRetornarListaEventosVacia() {
        when(eventoRepository.findAll()).thenReturn(Collections.emptyList());

        List<Evento> eventos = eventoService.getAllEvento();

        assertNotNull(eventos);
        assertTrue(eventos.isEmpty());
        verify(eventoRepository, times(1)).findAll();
    }
}
