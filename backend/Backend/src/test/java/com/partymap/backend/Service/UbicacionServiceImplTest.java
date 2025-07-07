package com.partymap.backend.service;

import com.partymap.backend.model.Ubicacion;
import com.partymap.backend.repository.UbicacionRepository;
import com.partymap.backend.service.Impl.UbicacionServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UbicacionServiceImplTest {

    private UbicacionRepository ubicacionRepository;
    private UbicacionServiceImpl ubicacionService;

    @BeforeEach
    void setUp() {
        ubicacionRepository = mock(UbicacionRepository.class);
        ubicacionService = new UbicacionServiceImpl(ubicacionRepository);
    }

    @Test
    void deberiaRetornarListaUbicacionesVacia() {
        when(ubicacionRepository.findAll()).thenReturn(Collections.emptyList());

        List<Ubicacion> ubicaciones = ubicacionService.getAllUbicaciones();

        assertNotNull(ubicaciones);
        assertTrue(ubicaciones.isEmpty());
        verify(ubicacionRepository, times(1)).findAll();
    }
}