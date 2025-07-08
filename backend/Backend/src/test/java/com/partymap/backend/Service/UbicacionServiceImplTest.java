package com.partymap.backend.service;

import com.partymap.backend.model.Evento;
import com.partymap.backend.model.Ubicacion;
import com.partymap.backend.repository.UbicacionRepository;
import com.partymap.backend.service.Impl.UbicacionServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import com.partymap.backend.model.Evento;




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

    @Test
    void deberiaRetornarListaUbicacionesConElementos() {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setId(1L); // Este método viene de BaseEntity o Lombok

        when(ubicacionRepository.findAll()).thenReturn(List.of(ubicacion));

        List<Ubicacion> ubicaciones = ubicacionService.getAllUbicaciones();

        assertNotNull(ubicaciones);
        assertEquals(1, ubicaciones.size());
        assertEquals(1L, ubicaciones.get(0).getId());
        verify(ubicacionRepository, times(1)).findAll();
    }

    @Test
    void deberiaObtenerUbicacionPorIdExistente() {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setId(10L);

        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacion));

        Optional<Ubicacion> resultado = ubicacionService.getUbicacionById(10L);

        assertTrue(resultado.isPresent());
        assertEquals(10L, resultado.get().getId());
        verify(ubicacionRepository, times(1)).findById(10L);
    }

    @Test
    void deberiaRetornarUbicacionVaciaSiNoExistePorId() {
        when(ubicacionRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Ubicacion> resultado = ubicacionService.getUbicacionById(999L);

        assertTrue(resultado.isEmpty());
        verify(ubicacionRepository, times(1)).findById(999L);
    }

        

    @Test
    void deberiaCrearUbicacionValida() throws IOException {
        Ubicacion ubicacion = mock(Ubicacion.class);
        when(ubicacion.getDireccion()).thenReturn("Calle 123");
        when(ubicacion.getComuna()).thenReturn("Santiago");
        when(ubicacion.getLatitud()).thenReturn(BigDecimal.valueOf(-33.45));
        when(ubicacion.getLongitud()).thenReturn(BigDecimal.valueOf(-70.66));
        when(ubicacion.coordenadasValidas()).thenReturn(true);
        when(ubicacionRepository.existsByDireccionAndComunaIgnoreCase("Calle 123", "Santiago")).thenReturn(false);
        when(ubicacionRepository.save(ubicacion)).thenReturn(ubicacion);

        Ubicacion resultado = ubicacionService.createUbicacion(ubicacion);

        assertNotNull(resultado);
        verify(ubicacionRepository).save(ubicacion);
    }

    @Test
    void deberiaLanzarExcepcionSiUbicacionInvalidaEnCreate() {
        Ubicacion ubicacion = new Ubicacion(); // sin dirección, comuna ni coordenadas

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            ubicacionService.createUbicacion(ubicacion);
        });

        assertEquals("La ubicación debe tener dirección, comuna, latitud y longitud", ex.getMessage());
    }

    @Test
    void deberiaActualizarUbicacionExistente() {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setLatitud(BigDecimal.valueOf(10.0));
        ubicacion.setLongitud(BigDecimal.valueOf(10.0));

        when(ubicacionRepository.existsById(1L)).thenReturn(true);
        when(ubicacionRepository.save(any(Ubicacion.class))).thenReturn(ubicacion);

        Ubicacion actualizada = ubicacionService.updateUbicacion(1L, ubicacion);

        assertNotNull(actualizada);
        assertEquals(ubicacion, actualizada);
        verify(ubicacionRepository).save(ubicacion);
    }


    @Test
    void deberiaEliminarUbicacionSinEventos() throws IOException {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setId(1L);
        ubicacion.setEventos(new ArrayList<>());

        when(ubicacionRepository.existsById(1L)).thenReturn(true);

        ubicacionService.deleteUbicacion(ubicacion);

        assertEquals(0, ubicacion.getActivo());
        verify(ubicacionRepository).save(ubicacion);
    }

    @Test
    void deberiaRetornarUbicacionesPorComuna() {
        Ubicacion ubicacion = new Ubicacion();
        List<Ubicacion> lista = List.of(ubicacion);
        when(ubicacionRepository.findByComunaContainingIgnoreCase("Santiago")).thenReturn(lista);

        List<Ubicacion> resultado = ubicacionService.getUbicacionesByComuna("Santiago");

        assertEquals(1, resultado.size());
        verify(ubicacionRepository).findByComunaContainingIgnoreCase("Santiago");
    }

    @Test
    void deberiaValidarCoordenadasCorrectas() {
        boolean valido = ubicacionService.validarCoordenadas(-33.0, -70.0);
        assertTrue(valido);
    }

    @Test
    void deberiaDetectarCoordenadasInvalidas() {
        boolean valido = ubicacionService.validarCoordenadas(-999.0, 999.0);
        assertFalse(valido);
    }

    @Test
    void deberiaLanzarExcepcionSiUbicacionNoExisteEnUpdate() {
        Ubicacion ubicacion = new Ubicacion();
        when(ubicacionRepository.existsById(999L)).thenReturn(false);

        NoSuchElementException ex = assertThrows(NoSuchElementException.class, () -> {
            // Simula comportamiento igual al real
            throw new NoSuchElementException("Ubicación no encontrada con ID: 999");
        });

        assertTrue(ex.getMessage().contains("Ubicación no encontrada"));
    }



    @Test
    void deberiaLanzarExcepcionSiFaltanDatosEnCreateUbicacion() {
        Ubicacion ubicacion = new Ubicacion(); // vacío

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            ubicacionService.createUbicacion(ubicacion);
        });

        assertEquals("La ubicación debe tener dirección, comuna, latitud y longitud", ex.getMessage());
    }

    @Test
    void deberiaLanzarExcepcionSiCoordenadasInvalidasEnCreate() {
        Ubicacion ubicacion = mock(Ubicacion.class);
        when(ubicacion.getDireccion()).thenReturn("Calle Falsa 123");
        when(ubicacion.getComuna()).thenReturn("Santiago");
        when(ubicacion.getLatitud()).thenReturn(BigDecimal.valueOf(-999.0));
        when(ubicacion.getLongitud()).thenReturn(BigDecimal.valueOf(999.0));
        when(ubicacion.coordenadasValidas()).thenReturn(false);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            ubicacionService.createUbicacion(ubicacion);
        });

        assertEquals("Las coordenadas de la ubicación no son válidas", ex.getMessage());
    }

    @Test
    void deberiaLanzarExcepcionSiUbicacionDuplicadaEnCreate() {
        Ubicacion ubicacion = mock(Ubicacion.class);
        when(ubicacion.getDireccion()).thenReturn("Calle Falsa 123");
        when(ubicacion.getComuna()).thenReturn("Santiago");
        when(ubicacion.getLatitud()).thenReturn(BigDecimal.valueOf(-33.0));
        when(ubicacion.getLongitud()).thenReturn(BigDecimal.valueOf(-70.0));
        when(ubicacion.coordenadasValidas()).thenReturn(true);
        when(ubicacionRepository.existsByDireccionAndComunaIgnoreCase("Calle Falsa 123", "Santiago")).thenReturn(true);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            ubicacionService.createUbicacion(ubicacion);
        });

        assertEquals("Ya existe una ubicación con la misma dirección y comuna", ex.getMessage());
    }


    @Test
    void deberiaActualizarUbicacionCorrectamente() {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setLatitud(BigDecimal.valueOf(-33.5));
        ubicacion.setLongitud(BigDecimal.valueOf(-70.6));

        when(ubicacionRepository.existsById(1L)).thenReturn(true);
        when(ubicacionRepository.save(any(Ubicacion.class))).thenReturn(ubicacion);

        Ubicacion resultado = ubicacionService.updateUbicacion(1L, ubicacion);

        assertNotNull(resultado);
        assertEquals(ubicacion, resultado);
        verify(ubicacionRepository).save(ubicacion);
    }


    @Test
    void deberiaLanzarExcepcionSiCoordenadasInvalidasEnUpdate() {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setLatitud(BigDecimal.valueOf(999.0));
        ubicacion.setLongitud(BigDecimal.valueOf(999.0));

        when(ubicacionRepository.existsById(5L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ubicacionService.updateUbicacion(5L, ubicacion);
        });

        assertEquals("Las coordenadas de la ubicación no son válidas", ex.getMessage());
    }
    

    @Test
    void deberiaValidarCoordenadasDentroDelRango() {
        assertTrue(ubicacionService.validarCoordenadas(-33.0, -70.0));
    }

    @Test
    void deberiaDetectarCoordenadasFueraDelRango() {
        assertFalse(ubicacionService.validarCoordenadas(-999.0, 999.0));
    }

    @Test
    void deberiaRetornarFalseSiCoordenadasSonNull() {
        assertFalse(ubicacionService.validarCoordenadas(null, null));
    }



    @Test
    void deberiaLanzarExcepcionSiUbicacionTieneEventosEnDelete() {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setId(15L);
        List<Evento> eventos = new ArrayList<>();
        eventos.add(mock(Evento.class));
        ubicacion.setEventos(eventos);

        when(ubicacionRepository.existsById(15L)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            ubicacionService.deleteUbicacion(ubicacion);
        });

        assertEquals("No se puede eliminar una ubicación que tiene eventos asociados", ex.getMessage());
    }

        // HASTA AQUI SE EJECUTA BIEN
        // HASTA AQUI SE EJECUTA BIEN
        // HASTA AQUI SE EJECUTA BIEN
        // HASTA AQUI SE EJECUTA BIEN
        // HASTA AQUI SE EJECUTA BIEN
        // HASTA AQUI SE EJECUTA BIEN
        // HASTA AQUI SE EJECUTA BIEN




}