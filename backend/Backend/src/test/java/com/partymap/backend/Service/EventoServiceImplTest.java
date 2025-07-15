package com.partymap.backend.service;

import com.partymap.backend.model.Evento;
import com.partymap.backend.model.Ubicacion;
import com.partymap.backend.model.Usuario;
import com.partymap.backend.repository.EventoRepository;
import com.partymap.backend.repository.UbicacionRepository;
import com.partymap.backend.service.impl.EventoServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Optional;
import com.partymap.backend.exceptions.NotFoundException;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.*;


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


    @Test
    void testCreateEventoConUbicacion_Exitoso() throws IOException {
        Evento evento = new Evento();
        evento.setNombre("Fiesta");
        evento.setDescripcion("Una fiesta muy divertida.");
        evento.setFecha(LocalDateTime.now().plusDays(1));
        evento.setCapacidadMaxima(100);
        evento.setPrecioEntrada(BigDecimal.TEN);
        evento.setImagenUrl("http://imagen.com/foto.jpg");
        evento.setUsuario(new Usuario());

        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setDireccion("Calle Falsa 123");
        ubicacion.setComuna("Santiago");
        ubicacion.setLatitud(new BigDecimal("-33.4569"));
        ubicacion.setLongitud(new BigDecimal("-70.6483"));

        when(ubicacionRepository.save(any(Ubicacion.class))).thenReturn(ubicacion);
        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Evento result = eventoService.createEventoConUbicacion(evento, ubicacion);

        assertNotNull(result);
        assertEquals("Fiesta", result.getNombre());
        verify(ubicacionRepository).save(any(Ubicacion.class));
        verify(eventoRepository).save(any(Evento.class));
    }

    @Test
    void testCreateEventoConUbicacion_ConUbicacionNula() {
        Evento evento = new Evento();
        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            eventoService.createEventoConUbicacion(evento, null);
        });
        assertTrue(ex.getMessage().contains("ubicación no puede ser nula"));
    }

    @Test
    void testUpdateEvento_Exitoso() {
        Long id = 1L;
        Evento evento = new Evento();
        evento.setNombre("Actualizado");
        evento.setDescripcion("Descripción válida");
        evento.setFecha(LocalDateTime.now().plusDays(1));

        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setDireccion("Dir");
        ubicacion.setComuna("Com");
        ubicacion.setLatitud(new BigDecimal("1.0"));
        ubicacion.setLongitud(new BigDecimal("1.0"));

        evento.setUbicacion(ubicacion);

        when(eventoRepository.existsById(id)).thenReturn(true);
        when(ubicacionRepository.save(any(Ubicacion.class))).thenReturn(ubicacion);
        when(eventoRepository.save(any(Evento.class))).thenReturn(evento);

        Evento resultado = eventoService.updateEvento(id, evento);

        assertEquals("Actualizado", resultado.getNombre());
    }

    @Test
    void testUpdateEventoConUbicacion_Exitoso() {
        Long id = 1L;

        Evento eventoExistente = new Evento();
        eventoExistente.setId(id);
        eventoExistente.setUbicacion(new Ubicacion());

        Evento eventoNuevo = new Evento();
        eventoNuevo.setNombre("Nuevo Nombre");
        eventoNuevo.setDescripcion("Descripción válida y larga");
        eventoNuevo.setFecha(LocalDateTime.now());
        eventoNuevo.setCapacidadMaxima(50);
        eventoNuevo.setPrecioEntrada(BigDecimal.ONE);
        eventoNuevo.setImagenUrl("http://url.imagen");

        Ubicacion nuevaUbicacion = new Ubicacion();
        nuevaUbicacion.setDireccion("Nueva Dir");
        nuevaUbicacion.setComuna("Comuna");
        nuevaUbicacion.setLatitud(new BigDecimal("1.0"));
        nuevaUbicacion.setLongitud(new BigDecimal("1.0"));

        when(eventoRepository.findById(id)).thenReturn(Optional.of(eventoExistente));
        when(ubicacionRepository.save(any(Ubicacion.class))).thenReturn(nuevaUbicacion);
        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Evento actualizado = eventoService.updateEventoConUbicacion(id, eventoNuevo, nuevaUbicacion);

        assertEquals("Nuevo Nombre", actualizado.getNombre());
        verify(eventoRepository).save(any(Evento.class));
    }

    @Test
    void testDeleteEvento_Exitoso() throws IOException {
        Evento evento = new Evento();
        evento.setId(1L);
        evento.setActivo(1);

        when(eventoRepository.existsById(1L)).thenReturn(true);
        when(eventoRepository.save(any(Evento.class))).thenReturn(evento);

        eventoService.deleteEvento(evento);

        verify(eventoRepository).save(evento);
        assertEquals(0, evento.getActivo());
    }

    @Test
    void testGetEventosByUsuarioId_FiltraActivos() {
        Long userId = 10L;
        Evento eventoActivo = new Evento();
        eventoActivo.setActivo(1);
        Evento eventoInactivo = new Evento();
        eventoInactivo.setActivo(0);

        when(eventoRepository.findByUsuarioId(userId)).thenReturn(List.of(eventoActivo, eventoInactivo));

        List<Evento> resultado = eventoService.getEventosByUsuarioId(userId);

        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).getActivo());
    }


        
    
    @Test
    void testCreateEventoConUbicacion_UbicacionNula() {
        Evento evento = new Evento();
        evento.setUsuario(usuarioValido()); // 👈 necesario para que pase la validación de usuario

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            eventoService.createEventoConUbicacion(evento, null);
        });
        assertEquals("La ubicación no puede ser nula", ex.getMessage());
    }

    @Test
    void testCreateEventoConUbicacion_UbicacionCamposNulos() {
        Evento evento = new Evento();
        evento.setUsuario(usuarioValido()); // 👈

        Ubicacion ubicacion = new Ubicacion(); // todos los campos nulos

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            eventoService.createEventoConUbicacion(evento, ubicacion);
        });
        assertEquals("La ubicación debe tener dirección, comuna, latitud y longitud", ex.getMessage());
    }

    @Test
    void testCreateEventoConUbicacion_CoordenadasInvalidas() {
        Evento evento = new Evento();
        evento.setUsuario(usuarioValido()); // 👈

        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setDireccion("Dir");
        ubicacion.setComuna("Comuna");
        ubicacion.setLatitud(new BigDecimal("999")); // inválidas
        ubicacion.setLongitud(new BigDecimal("999"));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            eventoService.createEventoConUbicacion(evento, ubicacion);
        });
        assertEquals("Las coordenadas de la ubicación no son válidas", ex.getMessage());
    }

    @Test
    void testCreateEventoConUbicacion_NombreEventoVacio() {
        Evento evento = new Evento();
        evento.setUsuario(usuarioValido()); // 👈
        evento.setNombre("   "); // vacío
        evento.setDescripcion("Descripción válida de más de 10 caracteres");
        evento.setFecha(LocalDateTime.now());

        Ubicacion ubicacion = ubicacionValida();

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            eventoService.createEventoConUbicacion(evento, ubicacion);
        });
        assertEquals("El nombre del evento es obligatorio", ex.getMessage());
    }

    @Test
    void testCreateEventoConUbicacion_FechaNula() {
        Evento evento = new Evento();
        evento.setUsuario(usuarioValido()); // 👈
        evento.setNombre("Fiesta");
        evento.setDescripcion("Descripción válida de más de 10 caracteres");
        evento.setFecha(null);

        Ubicacion ubicacion = ubicacionValida();

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            eventoService.createEventoConUbicacion(evento, ubicacion);
        });
        assertEquals("La fecha del evento es obligatoria", ex.getMessage());
    }

    @Test
    void testCreateEventoConUbicacion_DescripcionMuyCorta() {
        Evento evento = new Evento();
        evento.setUsuario(usuarioValido()); // 👈
        evento.setNombre("Fiesta");
        evento.setDescripcion("corta");
        evento.setFecha(LocalDateTime.now());

        Ubicacion ubicacion = ubicacionValida();

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            eventoService.createEventoConUbicacion(evento, ubicacion);
        });
        assertEquals("La descripción debe tener al menos 10 caracteres", ex.getMessage());
    }

    @Test
    void testCreateEventoConUbicacion_DescripcionMuyLarga() {
        Evento evento = new Evento();
        evento.setUsuario(usuarioValido()); // 👈
        evento.setNombre("Fiesta");
        evento.setDescripcion("a".repeat(2001)); // 2001 caracteres
        evento.setFecha(LocalDateTime.now());

        Ubicacion ubicacion = ubicacionValida();

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            eventoService.createEventoConUbicacion(evento, ubicacion);
        });
        assertEquals("La descripción no puede exceder 2000 caracteres", ex.getMessage());
    }


    @Test
    void testGetEventoById_EventoExiste() {
        Long eventoId = 1L;
        Evento eventoMock = new Evento();
        eventoMock.setId(eventoId);
        eventoMock.setNombre("Fiesta de prueba");

        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoMock));

        Optional<Evento> resultado = eventoService.getEventoById(eventoId);

        assertTrue(resultado.isPresent());
        assertEquals(eventoId, resultado.get().getId());
        assertEquals("Fiesta de prueba", resultado.get().getNombre());
        verify(eventoRepository).findById(eventoId);
    }

    @Test
    void testGetEventoById_EventoNoExiste() {
        Long eventoId = 2L;
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.empty());

        Optional<Evento> resultado = eventoService.getEventoById(eventoId);

        assertFalse(resultado.isPresent());
        verify(eventoRepository).findById(eventoId);
    }


    @Test
    void testUpdateEventoConUbicacion_EventoNoExiste() {
        Long eventoId = 99L;
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(NotFoundException.class, () -> {
            eventoService.updateEventoConUbicacion(eventoId, new Evento(), new Ubicacion());
        });

        assertEquals("Evento no encontrado con ID: 99", ex.getMessage());
    }

        

    @Test
    void testUpdateEventoConUbicacion_UbicacionNula() {
        Long eventoId = 1L;
        Evento eventoExistente = new Evento();
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoExistente));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            eventoService.updateEventoConUbicacion(eventoId, new Evento(), null);
        });

        assertEquals("La ubicación no puede ser nula", ex.getMessage());
    }


        

        @Test
        void testUpdateEventoConUbicacion_UbicacionConCamposNulos() {
            Long eventoId = 1L;
            Evento eventoExistente = new Evento();
            when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoExistente));

            Ubicacion ubicacion = new Ubicacion(); // dirección, comuna, lat/lng nulos

            Exception ex = assertThrows(IllegalArgumentException.class, () -> {
                eventoService.updateEventoConUbicacion(eventoId, new Evento(), ubicacion);
            });

            assertEquals("La ubicación debe tener dirección, comuna, latitud y longitud", ex.getMessage());
        }


    @Test
    void testUpdateEventoConUbicacion_NombreVacio() {
        Long eventoId = 1L;
        Evento eventoExistente = new Evento();
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoExistente));

        Evento evento = new Evento();
        evento.setNombre("   ");
        evento.setDescripcion("Descripción válida");
        evento.setFecha(LocalDateTime.now());

        Ubicacion ubicacion = ubicacionValida();

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            eventoService.updateEventoConUbicacion(eventoId, evento, ubicacion);
        });

        assertEquals("El nombre del evento es obligatorio", ex.getMessage());
    }

    
    @Test
    void testUpdateEventoConUbicacion_FechaNula() {
        Long eventoId = 1L;
        Evento eventoExistente = new Evento();
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoExistente));

        Evento evento = new Evento();
        evento.setNombre("Fiesta");
        evento.setDescripcion("Descripción válida");
        evento.setFecha(null);

        Ubicacion ubicacion = ubicacionValida();

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            eventoService.updateEventoConUbicacion(eventoId, evento, ubicacion);
        });

        assertEquals("La fecha del evento es obligatoria", ex.getMessage());
    }

    

    @Test
    void testUpdateEventoConUbicacion_DescripcionNula() {
        Long eventoId = 1L;
        Evento eventoExistente = new Evento();
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoExistente));

        Evento evento = new Evento();
        evento.setNombre("Fiesta");
        evento.setDescripcion(null);  // o puedes probar con "   "
        evento.setFecha(LocalDateTime.now());

        Ubicacion ubicacion = ubicacionValida();

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            eventoService.updateEventoConUbicacion(eventoId, evento, ubicacion);
        });

        assertEquals("La descripción del evento es obligatoria", ex.getMessage());
    }


    @Test
    void testUpdateEventoConUbicacion_DescripcionMuyLarga() {
        Long eventoId = 1L;
        Evento eventoExistente = new Evento();
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoExistente));

        Evento evento = new Evento();
        evento.setNombre("Fiesta");
        evento.setDescripcion("a".repeat(2001)); // 2001 caracteres
        evento.setFecha(LocalDateTime.now());

        Ubicacion ubicacion = ubicacionValida();

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            eventoService.updateEventoConUbicacion(eventoId, evento, ubicacion);
        });

        assertEquals("La descripción no puede exceder 2000 caracteres", ex.getMessage());
    }

    // HASTA AQUI SE EJECUTA BIEN
    // HASTA AQUI SE EJECUTA BIEN
    // HASTA AQUI SE EJECUTA BIEN
    // HASTA AQUI SE EJECUTA BIEN


    @Test
    void testUpdateEventoConUbicacion_CoordenadasInvalidas() {
        Long eventoId = 1L;
        Evento eventoExistente = new Evento();
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoExistente));

        // MOCK de Ubicacion
        Ubicacion ubicacionMock = Mockito.mock(Ubicacion.class);
        when(ubicacionMock.getDireccion()).thenReturn("Dirección");
        when(ubicacionMock.getComuna()).thenReturn("Comuna");
        when(ubicacionMock.getLatitud()).thenReturn(new BigDecimal("999"));
        when(ubicacionMock.getLongitud()).thenReturn(new BigDecimal("999"));
        when(ubicacionMock.coordenadasValidas()).thenReturn(false); // ✅ Esto ahora funcionará

        Evento evento = new Evento();
        evento.setNombre("Fiesta");
        evento.setDescripcion("Descripción válida");
        evento.setFecha(LocalDateTime.now());

        Exception ex = assertThrows(IllegalArgumentException.class, () -> {
            eventoService.updateEventoConUbicacion(eventoId, evento, ubicacionMock);
        });

        assertEquals("Las coordenadas de la ubicación no son válidas", ex.getMessage());
    }



        private Usuario usuarioValido() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Usuario Prueba");
        usuario.setEmail("test@correo.com");
        return usuario;
    }

    private Ubicacion ubicacionValida() {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setDireccion("Av. Siempre Viva 123");
        ubicacion.setComuna("Springfield");
        ubicacion.setLatitud(new BigDecimal("-33.4569"));
        ubicacion.setLongitud(new BigDecimal("-70.6483"));
        return ubicacion;
    }


}
