package com.partymap.backend;

import com.partymap.backend.RestClients.ClienteRest;
import com.partymap.backend.Service.Impl.BffServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class BffServiceImplTest {

    private ClienteRest clienteRest;
    private BffServiceImpl bffService;

    @BeforeEach
    void setUp() {
        clienteRest = mock(ClienteRest.class);
        bffService = new BffServiceImpl(clienteRest);
    }

    @Test
    void deberiaCrearCorrectamente() {
        Map<String, String> body = Map.of("key", "value");
        when(clienteRest.create(body)).thenReturn("creado");

        String resultado = bffService.create(body);

        assertEquals("creado", resultado);
        verify(clienteRest, times(1)).create(body);
    }

    @Test
    void deberiaLeerCorrectamente() {
        when(clienteRest.read("123")).thenReturn("data");

        String resultado = bffService.read("123");

        assertEquals("data", resultado);
        verify(clienteRest, times(1)).read("123");
    }

    @Test
    void deberiaActualizarCorrectamente() {
        when(clienteRest.update("activo")).thenReturn("ok");

        String resultado = bffService.update("activo");

        assertEquals("ok", resultado);
        verify(clienteRest, times(1)).update("activo");
    }

    @Test
    void deberiaEliminarCorrectamente() {
        when(clienteRest.delete("token")).thenReturn("eliminado");

        String resultado = bffService.delete("token");

        assertEquals("eliminado", resultado);
        verify(clienteRest, times(1)).delete("token");
    }
}
