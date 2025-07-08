package com.partymap.backend.controller;

import com.partymap.backend.dto.UbicacionDTO;
import com.partymap.backend.dto.UbicacionResponseDTO;
import com.partymap.backend.model.Ubicacion;
import com.partymap.backend.service.UbicacionService;
import com.partymap.backend.exceptions.NotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UbicacionControllerTest {

    @Mock
    private UbicacionService ubicacionService;

    @InjectMocks
    private UbicacionController ubicacionController;

    private Ubicacion ubicacion;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ubicacion = new Ubicacion();
        ubicacion.setId(1L);
        ubicacion.setDireccion("Av. Siempre Viva 742");
        ubicacion.setComuna("Springfield");
        ubicacion.setLatitud(new BigDecimal("-33.45"));
        ubicacion.setLongitud(new BigDecimal("-70.66"));
        ubicacion.setActivo(1);
        ubicacion.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    void testGetAllUbicaciones() {
        when(ubicacionService.getAllUbicaciones()).thenReturn(List.of(ubicacion));

        ResponseEntity<List<UbicacionResponseDTO>> response = ubicacionController.getAllUbicaciones();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetUbicacionById_Existe() {
        when(ubicacionService.getUbicacionById(1L)).thenReturn(Optional.of(ubicacion));

        ResponseEntity<UbicacionResponseDTO> response = ubicacionController.getUbicacionById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Springfield", response.getBody().getComuna());
    }

    @Test
    void testGetUbicacionById_NoExiste() {
        when(ubicacionService.getUbicacionById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            ubicacionController.getUbicacionById(99L);
        });
    }

    @Test
    void testCreateUbicacion_Exitosa() throws IOException {
        UbicacionDTO dto = new UbicacionDTO();
        dto.setDireccion("Nueva Dirección");
        dto.setComuna("Comuna Nueva");
        dto.setLatitud(new BigDecimal("-33.45"));
        dto.setLongitud(new BigDecimal("-70.66"));

        when(ubicacionService.createUbicacion(any(Ubicacion.class))).thenReturn(ubicacion);

        ResponseEntity<UbicacionResponseDTO> response = ubicacionController.createUbicacion(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Springfield", response.getBody().getComuna());
    }


    @Test
    void testUpdateUbicacion_Exitosa() {
        UbicacionDTO dto = new UbicacionDTO();
        dto.setDireccion("Modificada");
        dto.setComuna("Modificada");
        dto.setLatitud(new BigDecimal("-33.1"));
        dto.setLongitud(new BigDecimal("-70.5"));

        when(ubicacionService.updateUbicacion(eq(1L), any(Ubicacion.class))).thenReturn(ubicacion);

        ResponseEntity<UbicacionResponseDTO> response = ubicacionController.updateUbicacion(1L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateUbicacion_NoEncontrada() {
        UbicacionDTO dto = new UbicacionDTO();
        dto.setDireccion("Calle Falsa");
        dto.setComuna("Falsa");

        when(ubicacionService.updateUbicacion(eq(99L), any(Ubicacion.class))).thenThrow(new NotFoundException("No existe"));

        ResponseEntity<UbicacionResponseDTO> response = ubicacionController.updateUbicacion(99L, dto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteUbicacion_Existe() {
        when(ubicacionService.getUbicacionById(1L)).thenReturn(Optional.of(ubicacion));

        ResponseEntity<Void> response = ubicacionController.deleteUbicacion(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeleteUbicacion_NoExiste() {
        when(ubicacionService.getUbicacionById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = ubicacionController.deleteUbicacion(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testBuscarUbicaciones_PorComuna() {
        Ubicacion otra = new Ubicacion();
        otra.setComuna("Providencia");
        otra.setDireccion("Nueva 123");

        when(ubicacionService.getAllUbicaciones()).thenReturn(List.of(ubicacion, otra));

        ResponseEntity<List<UbicacionResponseDTO>> response = ubicacionController.buscarUbicaciones("Springfield", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetUbicacionesByComuna() {
        when(ubicacionService.getUbicacionesByComuna("Providencia")).thenReturn(List.of(ubicacion));

        ResponseEntity<List<UbicacionResponseDTO>> response = ubicacionController.getUbicacionesByComuna("Providencia");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testValidarCoordenadas() {
        when(ubicacionService.validarCoordenadas(-33.44, -70.66)).thenReturn(true);

        ResponseEntity<Boolean> response = ubicacionController.validarCoordenadas(-33.44, -70.66);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }
}
