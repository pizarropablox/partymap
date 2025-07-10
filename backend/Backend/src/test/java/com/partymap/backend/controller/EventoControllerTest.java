package com.partymap.backend.controller;

import com.partymap.backend.dto.EventoConUbicacionDTO;
import com.partymap.backend.dto.EventoConUbicacionUpdateDTO;
import com.partymap.backend.dto.EventoDTO;
import com.partymap.backend.dto.EventoResponseDTO;
import com.partymap.backend.dto.UbicacionDTO;
import com.partymap.backend.exceptions.NotFoundException;
import com.partymap.backend.model.Evento;
import com.partymap.backend.model.Ubicacion;
import com.partymap.backend.model.Usuario;
import com.partymap.backend.service.EventoService;
import com.partymap.backend.config.SecurityUtils;
import com.partymap.backend.repository.UbicacionRepository;
import com.partymap.backend.repository.UsuarioRepository;
import java.lang.reflect.Method;
import com.partymap.backend.controller.EventoController;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.partymap.backend.model.Evento;
import com.partymap.backend.model.Usuario;
import com.partymap.backend.model.TipoUsuario;




import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


public class EventoControllerTest {


    @Mock
    private EventoService eventoService;

    @Mock
    private UbicacionRepository ubicacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private EventoController eventoController;

    //private EventoController controller;

    private Evento evento1;
    private Evento evento2;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        evento1 = new Evento();
        evento1.setId(1L);
        evento1.setNombre("Fiesta 1");

        evento2 = new Evento();
        evento2.setId(2L);
        evento2.setNombre("Fiesta 2");
    }

    // Método auxiliar para setear campos privados vía reflexión
    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    


    @Test
    void testGetAllEventos_OK() {
        when(eventoService.getAllEvento()).thenReturn(List.of(evento1, evento2));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.getAllEventos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Fiesta 1", response.getBody().get(0).getNombre());
    }

    @Test
    void testGetEventoById_OK() {
        when(eventoService.getEventoById(1L)).thenReturn(Optional.of(evento1));

        ResponseEntity<EventoResponseDTO> response = eventoController.getEventoById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fiesta 1", response.getBody().getNombre());
    }

    @Test
    void testGetEventoById_NotFound() {
        when(eventoService.getEventoById(99L)).thenReturn(Optional.empty());

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> {
            eventoController.getEventoById(99L);
        });

        assertEquals("Evento no encontrado con ID: 99", thrown.getMessage());
    }

    //FUNCIONANDO

    @Test
    void testCreateEventoConUbicacion_NoAutenticado() throws Exception {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        EventoConUbicacionDTO dto = new EventoConUbicacionDTO();

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(dto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    //FUNCIONANDO

    @Test
    void testCreateEventoConUbicacion_UsuarioSinPermiso() throws Exception {
        Usuario user = new Usuario();
        setField(user, "rolAzure", "CLIENTE");

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(user));

        EventoConUbicacionDTO dto = new EventoConUbicacionDTO();

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(dto);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    //FUNCIONANDO

    @Test
    void testCreateEvento_OK() throws IOException {
        EventoDTO dto = new EventoDTO();
        Evento mockEvento = new Evento();

        when(eventoService.createEvento(any(Evento.class))).thenReturn(mockEvento);

        ResponseEntity<?> response = eventoController.createEvento(dto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void testConvertToEntityWithoutUsuario() throws Exception {
        EventoDTO dto = new EventoDTO();
        dto.setId(1L);
        dto.setNombre("Fiesta");
        dto.setDescripcion("Reggaeton toda la noche");
        dto.setFecha(LocalDateTime.now());
        dto.setCapacidadMaxima(100);
        dto.setPrecioEntrada(BigDecimal.valueOf(7000.0));
        dto.setImagenUrl("imagen.jpg");

        EventoController controller = org.mockito.Mockito.mock(EventoController.class);
        Method method = EventoController.class.getDeclaredMethod("convertToEntityWithoutUsuario", EventoDTO.class);
        method.setAccessible(true);
        Evento resultado = (Evento) method.invoke(controller, dto);

        assertNotNull(resultado);
        assertEquals(dto.getNombre(), resultado.getNombre());
    }

    @Test
    void testConvertUbicacionToEntity() throws Exception {
        UbicacionDTO dto = new UbicacionDTO();
        dto.setId(2L);
        dto.setDireccion("Av. Siempre Viva 123");
        dto.setComuna("Santiago");
        dto.setLatitud(BigDecimal.valueOf(-33.45));
        dto.setLongitud(BigDecimal.valueOf(-70.66));

        EventoController controller = org.mockito.Mockito.mock(EventoController.class);
        Method method = EventoController.class.getDeclaredMethod("convertUbicacionToEntity", UbicacionDTO.class);
        method.setAccessible(true);
        Ubicacion resultado = (Ubicacion) method.invoke(controller, dto);

        assertNotNull(resultado);
        assertEquals(dto.getDireccion(), resultado.getDireccion());
    }

    // FUNCIONANDO


    @Test
    void testGetEventoByIdWithUsuario() {
        // Crear un usuario simulado
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");
        usuario.setEmail("juan@test.com");
        usuario.setRutProductor("12345678-9");

        // Crear un evento simulado con usuario
        Evento evento = new Evento();
        evento.setId(100L);
        evento.setUsuario(usuario);

        // Simular comportamiento del servicio
        when(eventoService.getEventoById(100L)).thenReturn(Optional.of(evento));

        // Ejecutar el método del controlador
        ResponseEntity<EventoResponseDTO> response = eventoController.getEventoById(100L);

        // Validar respuesta
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        EventoResponseDTO dto = response.getBody();
        assertNotNull(dto);

        // Validar que se copió información del usuario
        assertEquals(1L, dto.getUsuarioId());
        assertEquals("Juan", dto.getUsuarioNombre());
        assertEquals("juan@test.com", dto.getUsuarioEmail());
        assertEquals("12345678-9", dto.getUsuarioRutProductor());
    }

    // FUNCIONANDO

    @Test
    void testGetEventosDisponibles() {
        Evento evento = new Evento();
        evento.setActivo(1);
        evento.setCapacidadMaxima(100);
        evento.setReservas(List.of()); // sin reservas
        evento.setFecha(LocalDateTime.now().plusDays(1));
        when(eventoService.getAllEvento()).thenReturn(List.of(evento));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.getEventosDisponibles();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // FUNCIONANDO

    @Test
    void testGetEventosProximos() {
        Evento evento = new Evento();
        evento.setFecha(LocalDateTime.now().plusHours(10));
        when(eventoService.getAllEvento()).thenReturn(List.of(evento));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.getEventosProximos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    // FUNCIONANDO

    @Test
    void testGetCuposDisponibles_OK() {
        Evento evento = new Evento();
        evento.setCapacidadMaxima(100);
        evento.setReservas(List.of()); // 0 reservas

        when(eventoService.getEventoById(1L)).thenReturn(Optional.of(evento));

        ResponseEntity<Integer> response = eventoController.getCuposDisponibles(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(100, response.getBody());
    }

    // FUNCIONANDO

   @Test
    void testGetCuposDisponiblesConEventoMockeado() {
        Evento evento = mock(Evento.class);
        when(evento.getCuposDisponibles()).thenReturn(20);
        when(eventoService.getEventoById(1L)).thenReturn(Optional.of(evento));

        ResponseEntity<Integer> response = eventoController.getCuposDisponibles(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(20, response.getBody());
    }

    @Test
    void testBuscarEventosSinParametros() {
        when(eventoService.getAllEvento()).thenReturn(List.of());

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.buscarEventos(null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // FUNCIONANDO

    @Test
    void testConvertToResponseDTOConUbicacion_Reflejado() throws Exception {
        Evento evento = new Evento();
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setId(100L);
        ubicacion.setDireccion("Av. Siempre Viva 742");
        ubicacion.setComuna("Springfield");
        ubicacion.setLatitud(BigDecimal.valueOf(-33.456));
        ubicacion.setLongitud(BigDecimal.valueOf(-70.678));
        ubicacion.setActivo(1);
        ubicacion.setFechaCreacion(LocalDateTime.now());

        evento.setUbicacion(ubicacion);

        Method method = EventoController.class.getDeclaredMethod("convertToResponseDTO", Evento.class);
        method.setAccessible(true);

        EventoResponseDTO dto = (EventoResponseDTO) method.invoke(eventoController, evento);

        assertNotNull(dto.getUbicacion());
        assertEquals("Springfield", dto.getUbicacion().getComuna());
        assertEquals("Av. Siempre Viva 742", dto.getUbicacion().getDireccion());
    }

    // FUNCIONANDO

    @Test
    void testGetMisEstadisticasComoAdmin() {
        Usuario admin = mock(Usuario.class);
        when(admin.getId()).thenReturn(1L);
        when(admin.isAdministrador()).thenReturn(true);

        Evento evento1 = new Evento();
        evento1.setActivo(1);
        evento1.setFecha(LocalDateTime.now().plusDays(1));  // evento futuro

        Evento evento2 = new Evento();
        evento2.setActivo(0);
        evento2.setFecha(LocalDateTime.now().minusDays(1));  // evento pasado

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(eventoService.getAllEvento()).thenReturn(List.of(evento1, evento2));

        ResponseEntity<Object> response = eventoController.getMisEstadisticas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    //  FUNCIONANDO

    @Test
    void testUpdateEventoComoAdmin() {
        // Simula un administrador autenticado
        Usuario admin = mock(Usuario.class);
        when(admin.isAdministrador()).thenReturn(true);
        when(admin.isProductor()).thenReturn(false);

        // Simula un EventoDTO entrante
        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setNombre("Nuevo nombre");

        // Simula la conversión y la actualización
        Evento evento = new Evento();
        Evento eventoActualizado = new Evento();
        eventoActualizado.setNombre("Nuevo nombre");

        // Simula retorno del usuario autenticado y lógica de servicio
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(eventoService.updateEvento(eq(1L), any(Evento.class))).thenReturn(eventoActualizado);

        // Llama al método real
        ResponseEntity<EventoResponseDTO> response = eventoController.updateEvento(1L, eventoDTO);

        // Aserciones básicas
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Nuevo nombre", response.getBody().getNombre());
    }

    // FUNCIONANDO

    @Test
    void testGetMisEventosComoAdmin() {
        // Simula un usuario administrador
        Usuario admin = mock(Usuario.class);
        when(admin.isAdministrador()).thenReturn(true);
        when(admin.isProductor()).thenReturn(false);

        // Simula eventos retornados por el servicio
        Evento evento1 = new Evento();
        evento1.setId(1L);
        evento1.setNombre("Evento A");

        Evento evento2 = new Evento();
        evento2.setId(2L);
        evento2.setNombre("Evento B");

        List<Evento> eventos = List.of(evento1, evento2);

        // Simula el usuario autenticado y el retorno del servicio
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(eventoService.getAllEvento()).thenReturn(eventos);

        // Ejecuta el método
        ResponseEntity<List<EventoResponseDTO>> response = eventoController.getMisEventos();

        // Verifica que responde con OK y eventos
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Evento A", response.getBody().get(0).getNombre());
    }

    // FUNCIONANDO

    @Test
    void testGetMisEventosNoAutenticado() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.getMisEventos();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void testGetMisEventosClienteSinPermiso() {
        Usuario cliente = new Usuario() {
            @Override
            public boolean isAdministrador() {
                return false;
            }

            @Override
            public boolean isProductor() {
                return false;
            }
        };
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(cliente));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.getMisEventos();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testGetMisEventosComoProductor() {
        Usuario productor = new Usuario() {
            @Override
            public boolean isAdministrador() {
                return false;
            }

            @Override
            public boolean isProductor() {
                return true;
            }

            @Override
            public Long getId() {
                return 3L; // ID fijo para verificar que el evento es del productor
            }
        };

        Evento evento = new Evento();
        evento.setId(100L);
        evento.setNombre("Evento del productor");
        evento.setUsuario(productor);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(productor));
        when(eventoService.getAllEvento()).thenReturn(List.of(evento));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.getMisEventos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    // FUNCIONANDO

    @Test
    void testCreateEventoConUbicacion_NombreNulo() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();

        EventoDTO evento = new EventoDTO();
        evento.setNombre(null);
        evento.setFecha(LocalDateTime.now().plusDays(1));
        evento.setDescripcion("Descripción válida");
        request.setEvento(evento);

        UbicacionDTO ubicacion = new UbicacionDTO();
        ubicacion.setDireccion("Dirección válida");
        ubicacion.setComuna("Comuna válida");
        ubicacion.setLatitud(BigDecimal.valueOf(-33.45));
        ubicacion.setLongitud(BigDecimal.valueOf(-70.66));
        request.setUbicacion(ubicacion);

        Usuario usuarioMock = mock(Usuario.class);
        when(usuarioMock.isProductor()).thenReturn(true);
        when(usuarioMock.isAdministrador()).thenReturn(false);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioMock));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreateEventoConUbicacion_NombreVacio() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();

        EventoDTO evento = new EventoDTO();
        evento.setNombre(" ");
        evento.setFecha(LocalDateTime.now().plusDays(1));
        evento.setDescripcion("Descripción válida");
        request.setEvento(evento);

        UbicacionDTO ubicacion = new UbicacionDTO();
        ubicacion.setDireccion("Dirección válida");
        ubicacion.setComuna("Comuna válida");
        ubicacion.setLatitud(BigDecimal.valueOf(-33.45));
        ubicacion.setLongitud(BigDecimal.valueOf(-70.66));
        request.setUbicacion(ubicacion);

        Usuario usuarioMock = mock(Usuario.class);
        when(usuarioMock.isProductor()).thenReturn(true);
        when(usuarioMock.isAdministrador()).thenReturn(false);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioMock));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreateEventoConUbicacion_NombreMuyCorto() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();

        EventoDTO evento = new EventoDTO();
        evento.setNombre("AB");
        evento.setFecha(LocalDateTime.now().plusDays(1));
        evento.setDescripcion("Descripción válida");
        request.setEvento(evento);

        UbicacionDTO ubicacion = new UbicacionDTO();
        ubicacion.setDireccion("Dirección válida");
        ubicacion.setComuna("Comuna válida");
        ubicacion.setLatitud(BigDecimal.valueOf(-33.45));
        ubicacion.setLongitud(BigDecimal.valueOf(-70.66));
        request.setUbicacion(ubicacion);

        Usuario usuarioMock = mock(Usuario.class);
        when(usuarioMock.isProductor()).thenReturn(true);
        when(usuarioMock.isAdministrador()).thenReturn(false);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioMock));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreateEventoConUbicacion_NombreMuyLargo() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();

        EventoDTO evento = new EventoDTO();
        evento.setNombre("A".repeat(101));
        evento.setFecha(LocalDateTime.now().plusDays(1));
        evento.setDescripcion("Descripción válida");
        request.setEvento(evento);

        UbicacionDTO ubicacion = new UbicacionDTO();
        ubicacion.setDireccion("Dirección válida");
        ubicacion.setComuna("Comuna válida");
        ubicacion.setLatitud(BigDecimal.valueOf(-33.45));
        ubicacion.setLongitud(BigDecimal.valueOf(-70.66));
        request.setUbicacion(ubicacion);

        Usuario usuarioMock = mock(Usuario.class);
        when(usuarioMock.isProductor()).thenReturn(true);
        when(usuarioMock.isAdministrador()).thenReturn(false);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioMock));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreateEventoConUbicacion_FechaNula() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();

        EventoDTO evento = new EventoDTO();
        evento.setNombre("Nombre válido");
        evento.setFecha(null);
        evento.setDescripcion("Descripción válida");
        request.setEvento(evento);

        UbicacionDTO ubicacion = new UbicacionDTO();
        ubicacion.setDireccion("Dirección válida");
        ubicacion.setComuna("Comuna válida");
        ubicacion.setLatitud(BigDecimal.valueOf(-33.45));
        ubicacion.setLongitud(BigDecimal.valueOf(-70.66));
        request.setUbicacion(ubicacion);

        Usuario usuarioMock = mock(Usuario.class);
        when(usuarioMock.isProductor()).thenReturn(true);
        when(usuarioMock.isAdministrador()).thenReturn(false);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuarioMock));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void testCreateEventoConUbicacion_FechaPasada() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();

        EventoDTO evento = new EventoDTO();
        evento.setNombre("Nombre válido");
        evento.setFecha(LocalDateTime.now().minusDays(1));
        evento.setDescripcion("Descripción válida");
        request.setEvento(evento);

        UbicacionDTO ubicacion = new UbicacionDTO();
        request.setUbicacion(ubicacion);

        Usuario usuario = mock(Usuario.class);
        when(usuario.isProductor()).thenReturn(true);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreateEventoConUbicacion_DescripcionNula() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();

        EventoDTO evento = new EventoDTO();
        evento.setNombre("Nombre válido");
        evento.setFecha(LocalDateTime.now().plusDays(1));
        evento.setDescripcion(null);
        request.setEvento(evento);

        UbicacionDTO ubicacion = new UbicacionDTO();
        request.setUbicacion(ubicacion);

        Usuario usuario = mock(Usuario.class);
        when(usuario.isProductor()).thenReturn(true);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreateEventoConUbicacion_DescripcionMuyCorta() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();

        EventoDTO evento = new EventoDTO();
        evento.setNombre("Nombre válido");
        evento.setFecha(LocalDateTime.now().plusDays(1));
        evento.setDescripcion("Corta");
        request.setEvento(evento);

        UbicacionDTO ubicacion = new UbicacionDTO();
        request.setUbicacion(ubicacion);

        Usuario usuario = mock(Usuario.class);
        when(usuario.isProductor()).thenReturn(true);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreateEventoConUbicacion_DescripcionMuyLarga() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();

        EventoDTO evento = new EventoDTO();
        evento.setNombre("Nombre válido");
        evento.setFecha(LocalDateTime.now().plusDays(1));
        evento.setDescripcion("A".repeat(2001));
        request.setEvento(evento);

        UbicacionDTO ubicacion = new UbicacionDTO();
        request.setUbicacion(ubicacion);

        Usuario usuario = mock(Usuario.class);
        when(usuario.isProductor()).thenReturn(true);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreateEventoConUbicacion_SinUbicacion() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();

        EventoDTO evento = new EventoDTO();
        evento.setNombre("Nombre válido");
        evento.setFecha(LocalDateTime.now().plusDays(1));
        evento.setDescripcion("Descripción válida");
        request.setEvento(evento);
        request.setUbicacion(null); // sin ubicación

        Usuario usuario = mock(Usuario.class);
        when(usuario.isProductor()).thenReturn(true);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void testCreateEventoConUbicacion_DireccionNula() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();
        EventoDTO evento = new EventoDTO();
        evento.setNombre("Evento válido");
        evento.setFecha(LocalDateTime.now().plusDays(1));
        evento.setDescripcion("Descripción válida");
        request.setEvento(evento);

        UbicacionDTO ubicacion = new UbicacionDTO();
        ubicacion.setDireccion(null);
        request.setUbicacion(ubicacion);

        Usuario usuario = new Usuario();
        usuario.setTipoUsuario(TipoUsuario.PRODUCTOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreateEventoConUbicacion_DireccionMuyCorta() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();
        EventoDTO evento = new EventoDTO();
        evento.setNombre("Evento válido");
        evento.setFecha(LocalDateTime.now().plusDays(1));
        evento.setDescripcion("Descripción válida");
        request.setEvento(evento);

        UbicacionDTO ubicacion = new UbicacionDTO();
        ubicacion.setDireccion("1234");  // Menos de 5 caracteres
        request.setUbicacion(ubicacion);

        Usuario usuario = new Usuario();
        usuario.setTipoUsuario(TipoUsuario.PRODUCTOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreateEventoConUbicacion_ComunaNula() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();
        EventoDTO evento = new EventoDTO();
        evento.setNombre("Evento válido");
        evento.setFecha(LocalDateTime.now().plusDays(1));
        evento.setDescripcion("Descripción válida");
        request.setEvento(evento);

        UbicacionDTO ubicacion = new UbicacionDTO();
        ubicacion.setDireccion("Dirección válida");
        ubicacion.setComuna(null);
        request.setUbicacion(ubicacion);

        Usuario usuario = new Usuario();
        usuario.setTipoUsuario(TipoUsuario.PRODUCTOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreateEventoConUbicacion_ComunaMuyCorta() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();
        EventoDTO evento = new EventoDTO();
        evento.setNombre("Evento válido");
        evento.setFecha(LocalDateTime.now().plusDays(1));
        evento.setDescripcion("Descripción válida");
        request.setEvento(evento);

        UbicacionDTO ubicacion = new UbicacionDTO();
        ubicacion.setDireccion("Dirección válida");
        ubicacion.setComuna("A");  // Menos de 2 caracteres
        request.setUbicacion(ubicacion);

        Usuario usuario = new Usuario();
        usuario.setTipoUsuario(TipoUsuario.PRODUCTOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreateEventoConUbicacion_LatitudNula() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();
        EventoDTO evento = new EventoDTO();
        evento.setNombre("Evento válido");
        evento.setFecha(LocalDateTime.now().plusDays(1));
        evento.setDescripcion("Descripción válida");
        request.setEvento(evento);

        UbicacionDTO ubicacion = new UbicacionDTO();
        ubicacion.setDireccion("Dirección válida");
        ubicacion.setComuna("Comuna válida");
        ubicacion.setLatitud(null);
        ubicacion.setLongitud(new BigDecimal("-70.5"));
        request.setUbicacion(ubicacion);

        Usuario usuario = new Usuario();
        usuario.setTipoUsuario(TipoUsuario.PRODUCTOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCreateEventoConUbicacion_LongitudNula() {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();
        EventoDTO evento = new EventoDTO();
        evento.setNombre("Evento válido");
        evento.setFecha(LocalDateTime.now().plusDays(1));
        evento.setDescripcion("Descripción válida");
        request.setEvento(evento);

        UbicacionDTO ubicacion = new UbicacionDTO();
        ubicacion.setDireccion("Dirección válida");
        ubicacion.setComuna("Comuna válida");
        ubicacion.setLatitud(new BigDecimal("-33.4"));
        ubicacion.setLongitud(null);
        request.setUbicacion(ubicacion);

        Usuario usuario = new Usuario();
        usuario.setTipoUsuario(TipoUsuario.PRODUCTOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void testGetEventosPorUsuario_UsuarioNoAutenticado() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.getEventosPorUsuario(1L);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetEventosPorUsuario_UsuarioProductorNoExiste() {
        Usuario user = new Usuario();
        user.setId(1L);
        user.setTipoUsuario(TipoUsuario.CLIENTE);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(user));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            eventoController.getEventosPorUsuario(2L)
        );
        assertTrue(exception.getMessage().contains("Usuario productor no encontrado"));
    }

    @Test
    void testGetEventosPorUsuario_ProductorSinPermisoParaVerOtroProductor() {
        Usuario productorActual = new Usuario();
        productorActual.setId(1L);
        productorActual.setTipoUsuario(TipoUsuario.PRODUCTOR);

        Usuario otroProductor = new Usuario();
        otroProductor.setId(2L);
        otroProductor.setTipoUsuario(TipoUsuario.PRODUCTOR);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(productorActual));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(otroProductor));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.getEventosPorUsuario(2L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testGetEventosPorUsuario_AccesoValido_AdminPuedeVerCualquiera() {
        Usuario admin = new Usuario();
        admin.setId(10L);
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);

        Usuario productor = new Usuario();
        productor.setId(2L);
        productor.setTipoUsuario(TipoUsuario.PRODUCTOR);

        Evento evento = new Evento();
        evento.setUsuario(productor);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(productor));
        when(eventoService.getAllEvento()).thenReturn(List.of(evento));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.getEventosPorUsuario(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetEventosPorProductor_SinAutenticacion() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.getEventosPorProductor(1L);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void testBuscarMisEventos_UsuarioNoAutenticado() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.buscarMisEventos(null, null, null, null, null, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testBuscarMisEventos_ClienteSinPermiso() {
        Usuario cliente = new Usuario();
        cliente.setId(1L);
        cliente.setTipoUsuario(TipoUsuario.CLIENTE);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(cliente));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.buscarMisEventos(null, null, null, null, null, null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testBuscarMisEventos_AdminSinFiltros() {
        Usuario admin = new Usuario();
        admin.setId(1L);
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);

        Evento evento1 = new Evento();
        evento1.setId(10L);
        evento1.setNombre("Fiesta electrónica");
        evento1.setActivo(1);

        Evento evento2 = new Evento();
        evento2.setId(20L);
        evento2.setNombre("Cumbia Fest");
        evento2.setActivo(1);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(eventoService.getAllEvento()).thenReturn(List.of(evento1, evento2));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.buscarMisEventos(null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testBuscarMisEventos_ProductorFiltraPorNombre() {
        Usuario productor = new Usuario();
        productor.setId(1L);
        productor.setTipoUsuario(TipoUsuario.PRODUCTOR);

        Evento evento1 = new Evento();
        evento1.setId(1L);
        evento1.setNombre("Evento Tech");
        evento1.setActivo(1);
        evento1.setUsuario(productor);

        Evento evento2 = new Evento();
        evento2.setId(2L);
        evento2.setNombre("Cumbia Fest");
        evento2.setActivo(1);
        evento2.setUsuario(productor);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(productor));
        when(eventoService.getAllEvento()).thenReturn(List.of(evento1, evento2));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.buscarMisEventos("tech", null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getNombre().toLowerCase().contains("tech"));
    }

    @Test
    void testBuscarMisEventos_ProductorFiltraPorComunaYActivos() {
        Usuario productor = new Usuario();
        productor.setId(1L);
        productor.setTipoUsuario(TipoUsuario.PRODUCTOR);

        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setComuna("Ñuñoa");

        Evento evento1 = new Evento();
        evento1.setId(1L);
        evento1.setNombre("Feria local");
        evento1.setActivo(1);
        evento1.setUsuario(productor);
        evento1.setUbicacion(ubicacion);

        Evento evento2 = new Evento();
        evento2.setId(2L);
        evento2.setNombre("Concierto");
        evento2.setActivo(0);
        evento2.setUsuario(productor);
        evento2.setUbicacion(ubicacion);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(productor));
        when(eventoService.getAllEvento()).thenReturn(List.of(evento1, evento2));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.buscarMisEventos(null, "ñuñoa", true, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Feria local", response.getBody().get(0).getNombre());
    }

    // FUNCIONANDO

    @Test
    void testUpdateEventoConUbicacion_NoAutenticado() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(1L, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testUpdateEventoConUbicacion_ClienteSinPermiso() {
        Usuario cliente = new Usuario();
        cliente.setId(1L);
        cliente.setTipoUsuario(TipoUsuario.CLIENTE);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(cliente));

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(1L, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testUpdateEventoConUbicacion_ProductorEventoDeOtro() {
        Usuario productor = new Usuario();
        productor.setId(1L);
        productor.setTipoUsuario(TipoUsuario.PRODUCTOR);

        Usuario otroProductor = new Usuario();
        otroProductor.setId(2L);

        Evento eventoExistente = new Evento();
        eventoExistente.setId(1L);
        eventoExistente.setUsuario(otroProductor);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(productor));
        when(eventoService.getEventoById(1L)).thenReturn(Optional.of(eventoExistente));

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(1L, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testUpdateEventoConUbicacion_NombreNulo() {
        Usuario admin = new Usuario();
        admin.setId(1L);
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);

        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setNombre(null);
        eventoDTO.setFecha(LocalDateTime.now().plusDays(1));
        eventoDTO.setDescripcion("Descripción válida");

        UbicacionDTO ubicacionDTO = new UbicacionDTO();
        ubicacionDTO.setDireccion("Dirección válida");
        ubicacionDTO.setComuna("Comuna válida");

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        request.setEvento(eventoDTO);
        request.setUbicacion(ubicacionDTO);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));

        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testDeleteEvento_NoAutenticado() {
        when(securityUtils.getCurrentUser()).thenReturn(Optional.empty());

        ResponseEntity<Void> response = eventoController.deleteEvento(1L);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testDeleteEvento_ClienteSinPermiso() {
        Usuario cliente = new Usuario();
        cliente.setId(1L);
        cliente.setTipoUsuario(TipoUsuario.CLIENTE);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(cliente));

        ResponseEntity<Void> response = eventoController.deleteEvento(1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testDeleteEvento_ProductorOtroEvento() {
        Usuario productor = new Usuario();
        productor.setId(1L);
        productor.setTipoUsuario(TipoUsuario.PRODUCTOR);

        Usuario otro = new Usuario();
        otro.setId(2L);

        Evento evento = new Evento();
        evento.setId(1L);
        evento.setUsuario(otro);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(productor));
        when(eventoService.getEventoById(1L)).thenReturn(Optional.of(evento));

        ResponseEntity<Void> response = eventoController.deleteEvento(1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testDeleteEvento_AdministradorPuedeEliminar() throws IOException {
        Usuario admin = new Usuario();
        admin.setId(1L);
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);

        Evento evento = new Evento();
        evento.setId(1L);
        evento.setUsuario(admin);

        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(eventoService.getEventoById(1L)).thenReturn(Optional.of(evento));

        ResponseEntity<Void> response = eventoController.deleteEvento(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(eventoService).deleteEvento(evento);
    }

    // FUNCIONANDO

    @Test
    void testUpdateEventoConUbicacion_NombreVacio() {
        Usuario admin = new Usuario();
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));

        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setNombre(" ");
        eventoDTO.setFecha(LocalDateTime.now().plusDays(1));
        eventoDTO.setDescripcion("Descripción válida");

        UbicacionDTO ubicacionDTO = new UbicacionDTO();
        ubicacionDTO.setDireccion("Dirección válida");
        ubicacionDTO.setComuna("Comuna válida");

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        request.setEvento(eventoDTO);
        request.setUbicacion(ubicacionDTO);

        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(1L, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateEventoConUbicacion_NombreMuyCorto() {
        Usuario admin = new Usuario();
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));

        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setNombre("AB");
        eventoDTO.setFecha(LocalDateTime.now().plusDays(1));
        eventoDTO.setDescripcion("Descripción válida");

        UbicacionDTO ubicacionDTO = new UbicacionDTO();
        ubicacionDTO.setDireccion("Dirección válida");
        ubicacionDTO.setComuna("Comuna válida");

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        request.setEvento(eventoDTO);
        request.setUbicacion(ubicacionDTO);

        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(1L, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateEventoConUbicacion_NombreMuyLargo() {
        Usuario admin = new Usuario();
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));

        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setNombre("A".repeat(101));
        eventoDTO.setFecha(LocalDateTime.now().plusDays(1));
        eventoDTO.setDescripcion("Descripción válida");

        UbicacionDTO ubicacionDTO = new UbicacionDTO();
        ubicacionDTO.setDireccion("Dirección válida");
        ubicacionDTO.setComuna("Comuna válida");

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        request.setEvento(eventoDTO);
        request.setUbicacion(ubicacionDTO);

        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(1L, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateEventoConUbicacion_FechaNula() {
        Usuario admin = new Usuario();
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));

        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setNombre("Nombre válido");
        eventoDTO.setFecha(null);
        eventoDTO.setDescripcion("Descripción válida");

        UbicacionDTO ubicacionDTO = new UbicacionDTO();
        ubicacionDTO.setDireccion("Dirección válida");
        ubicacionDTO.setComuna("Comuna válida");

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        request.setEvento(eventoDTO);
        request.setUbicacion(ubicacionDTO);

        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(1L, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateEventoConUbicacion_FechaPasada() {
        Usuario admin = new Usuario();
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));

        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setNombre("Nombre válido");
        eventoDTO.setFecha(LocalDateTime.now().minusDays(1));
        eventoDTO.setDescripcion("Descripción válida");

        UbicacionDTO ubicacionDTO = new UbicacionDTO();
        ubicacionDTO.setDireccion("Dirección válida");
        ubicacionDTO.setComuna("Comuna válida");

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        request.setEvento(eventoDTO);
        request.setUbicacion(ubicacionDTO);

        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(1L, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateEventoConUbicacion_DescripcionMuyCorta() {
        Usuario admin = new Usuario();
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));

        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setNombre("Nombre válido");
        eventoDTO.setFecha(LocalDateTime.now().plusDays(1));
        eventoDTO.setDescripcion("Corto");

        UbicacionDTO ubicacionDTO = new UbicacionDTO();
        ubicacionDTO.setDireccion("Dirección válida");
        ubicacionDTO.setComuna("Comuna válida");

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        request.setEvento(eventoDTO);
        request.setUbicacion(ubicacionDTO);

        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(1L, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateEventoConUbicacion_SinUbicacion() {
        Usuario admin = new Usuario();
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));

        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setNombre("Nombre válido");
        eventoDTO.setFecha(LocalDateTime.now().plusDays(1));
        eventoDTO.setDescripcion("Descripción válida");

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        request.setEvento(eventoDTO);
        request.setUbicacion(null); // ubicación faltante

        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(1L, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void testUpdateEventoConUbicacion_DireccionNula() {
        Usuario admin = new Usuario();
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));

        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setNombre("Evento válido");
        eventoDTO.setFecha(LocalDateTime.now().plusDays(1));
        eventoDTO.setDescripcion("Descripción válida");

        UbicacionDTO ubicacionDTO = new UbicacionDTO();
        ubicacionDTO.setDireccion(null);  // Dirección nula
        ubicacionDTO.setComuna("Comuna válida");

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        request.setEvento(eventoDTO);
        request.setUbicacion(ubicacionDTO);

        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(1L, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateEventoConUbicacion_DireccionMuyCorta() {
        Usuario admin = new Usuario();
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));

        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setNombre("Evento válido");
        eventoDTO.setFecha(LocalDateTime.now().plusDays(1));
        eventoDTO.setDescripcion("Descripción válida");

        UbicacionDTO ubicacionDTO = new UbicacionDTO();
        ubicacionDTO.setDireccion("1234");  // Menos de 5 caracteres
        ubicacionDTO.setComuna("Comuna válida");

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        request.setEvento(eventoDTO);
        request.setUbicacion(ubicacionDTO);

        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(1L, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateEventoConUbicacion_ComunaNula() {
        Usuario admin = new Usuario();
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));

        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setNombre("Evento válido");
        eventoDTO.setFecha(LocalDateTime.now().plusDays(1));
        eventoDTO.setDescripcion("Descripción válida");

        UbicacionDTO ubicacionDTO = new UbicacionDTO();
        ubicacionDTO.setDireccion("Dirección válida");
        ubicacionDTO.setComuna(null);  // Comuna nula

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        request.setEvento(eventoDTO);
        request.setUbicacion(ubicacionDTO);

        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(1L, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateEventoConUbicacion_ComunaMuyCorta() {
        Usuario admin = new Usuario();
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));

        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setNombre("Evento válido");
        eventoDTO.setFecha(LocalDateTime.now().plusDays(1));
        eventoDTO.setDescripcion("Descripción válida");

        UbicacionDTO ubicacionDTO = new UbicacionDTO();
        ubicacionDTO.setDireccion("Dirección válida");
        ubicacionDTO.setComuna("A");  // Menos de 2 caracteres

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        request.setEvento(eventoDTO);
        request.setUbicacion(ubicacionDTO);

        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(1L, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // FUNCIONANDO

    @Test
    void testCreateEventoConUbicacion_CreacionExitosa() throws Exception {
        EventoConUbicacionDTO request = new EventoConUbicacionDTO();
        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setNombre("Concierto");
        eventoDTO.setFecha(LocalDateTime.now().plusDays(1));
        eventoDTO.setDescripcion("Evento musical en vivo");
        request.setEvento(eventoDTO);

        UbicacionDTO ubicacionDTO = new UbicacionDTO();
        ubicacionDTO.setDireccion("Calle Falsa 123");
        ubicacionDTO.setComuna("Santiago");
        ubicacionDTO.setLatitud(BigDecimal.valueOf(-33.45));
        ubicacionDTO.setLongitud(BigDecimal.valueOf(-70.66));
        request.setUbicacion(ubicacionDTO);

        Usuario usuario = new Usuario();
        usuario.setTipoUsuario(TipoUsuario.PRODUCTOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        Evento eventoMock = new Evento();
        when(eventoService.createEventoConUbicacion(any(), any())).thenReturn(eventoMock);

        ResponseEntity<EventoResponseDTO> response = eventoController.createEventoConUbicacion(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testUpdateEventoConUbicacion_ActualizacionExitosa() throws Exception {
        Long eventoId = 1L;

        EventoConUbicacionUpdateDTO request = new EventoConUbicacionUpdateDTO();
        EventoDTO eventoDTO = new EventoDTO();
        eventoDTO.setNombre("Nuevo Evento");
        eventoDTO.setFecha(LocalDateTime.now().plusDays(2));
        eventoDTO.setDescripcion("Descripción actualizada");
        request.setEvento(eventoDTO);

        UbicacionDTO ubicacionDTO = new UbicacionDTO();
        ubicacionDTO.setDireccion("Nueva dirección 456");
        ubicacionDTO.setComuna("Providencia");
        ubicacionDTO.setLatitud(BigDecimal.valueOf(-33.42));
        ubicacionDTO.setLongitud(BigDecimal.valueOf(-70.60));
        request.setUbicacion(ubicacionDTO);

        Usuario usuario = new Usuario();
        usuario.setId(eventoId);
        usuario.setTipoUsuario(TipoUsuario.PRODUCTOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        Evento eventoExistente = new Evento();
        eventoExistente.setUsuario(usuario);
        when(eventoService.getEventoById(eventoId)).thenReturn(Optional.of(eventoExistente));
        when(eventoService.updateEventoConUbicacion(any(), any(), any())).thenReturn(eventoExistente);

        ResponseEntity<EventoResponseDTO> response = eventoController.updateEventoConUbicacion(eventoId, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testBuscarMisEventos_FiltrarPorNombre() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setTipoUsuario(TipoUsuario.PRODUCTOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        Evento evento = new Evento();
        evento.setNombre("Festival Primavera");
        evento.setUsuario(usuario);
        when(eventoService.getAllEvento()).thenReturn(List.of(evento));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.buscarMisEventos("primavera", null, null, null, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testBuscarMisEventos_FiltrarPorComuna() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setTipoUsuario(TipoUsuario.PRODUCTOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        Evento evento = new Evento();
        evento.setNombre("Evento A");
        evento.setUsuario(usuario);
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setComuna("Las Condes");
        evento.setUbicacion(ubicacion);
        when(eventoService.getAllEvento()).thenReturn(List.of(evento));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.buscarMisEventos(null, "condes", null, null, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testBuscarMisEventos_FiltrarPorUsuarioId() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setTipoUsuario(TipoUsuario.PRODUCTOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(usuario));

        Evento evento = new Evento();
        evento.setNombre("Evento X");
        Usuario otro = new Usuario();
        otro.setId(2L);
        evento.setUsuario(otro);
        when(eventoService.getAllEvento()).thenReturn(List.of(evento));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.buscarMisEventos(null, null, null, null, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }


    @Test
    void testGetEventosPorProductor_UsuarioNoExiste() {
        Usuario admin = new Usuario();
        admin.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            eventoController.getEventosPorProductor(999L);
        });
    }

    @Test
    void testGetEventosPorProductor_AccesoDenegadoProductor() {
        Usuario productor = new Usuario();
        productor.setId(1L);
        productor.setTipoUsuario(TipoUsuario.PRODUCTOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(productor));

        Usuario otro = new Usuario();
        otro.setId(2L);
        otro.setTipoUsuario(TipoUsuario.PRODUCTOR);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(otro));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.getEventosPorProductor(2L);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testFiltrarEventosPorUsuarioProductor() {
        Usuario productor = new Usuario();
        productor.setId(10L);
        productor.setTipoUsuario(TipoUsuario.PRODUCTOR);
        when(securityUtils.getCurrentUser()).thenReturn(Optional.of(productor));

        Evento e1 = new Evento();
        e1.setUsuario(productor);
        Evento e2 = new Evento();
        Usuario otro = new Usuario();
        otro.setId(20L);
        e2.setUsuario(otro);
        when(eventoService.getAllEvento()).thenReturn(List.of(e1, e2));

        ResponseEntity<List<EventoResponseDTO>> response = eventoController.buscarMisEventos(null, null, null, null, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    // FUNCIONANDO

    




}
