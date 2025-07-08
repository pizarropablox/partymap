package com.partymap.backend.controller;

import com.partymap.backend.dto.EventoConUbicacionDTO;
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


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.lang.reflect.Field;
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




}
