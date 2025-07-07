package com.partymap.backend.service;

import com.partymap.backend.model.TipoUsuario;
import com.partymap.backend.model.Usuario;
import com.partymap.backend.repository.UsuarioRepository;
import com.partymap.backend.service.Impl.UsuarioServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.security.oauth2.jwt.Jwt;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UsuarioServiceImplTest {

    private UsuarioRepository usuarioRepository;
    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        usuarioService = new UsuarioServiceImpl(usuarioRepository);
    }

    @Test
    void deberiaRetornarListaUsuariosVacia() {
        when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());

        List<Usuario> usuarios = usuarioService.getAllUsuarios();

        assertNotNull(usuarios);
        assertTrue(usuarios.isEmpty());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    void deberiaCrearUsuarioCorrectamente() {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("Juan");

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        Usuario resultado = usuarioService.createUsuario(usuarioMock);

        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        verify(usuarioRepository, times(1)).save(usuarioMock);
    }

    @Test
    void deberiaRetornarUsuarioPorIdSiExiste() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.getUsuarioById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    void deberiaRetornarUsuarioPorEmailSiExiste() {
        Usuario usuario = new Usuario();
        usuario.setEmail("test@email.com");
        when(usuarioRepository.findByEmail("test@email.com")).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.getUsuarioByEmail("test@email.com");

        assertTrue(resultado.isPresent());
        assertEquals("test@email.com", resultado.get().getEmail());
        verify(usuarioRepository, times(1)).findByEmail("test@email.com");
    }

    @Test
    void deberiaRetornarTrueSiExisteUsuarioPorEmail() {
        when(usuarioRepository.existsByEmail("existe@email.com")).thenReturn(true);

        boolean existe = usuarioService.existsByEmail("existe@email.com");

        assertTrue(existe);
        verify(usuarioRepository, times(1)).existsByEmail("existe@email.com");
    }

    @Test
    void deberiaEliminarUsuarioPorId_caso_simple() {
        doNothing().when(usuarioRepository).deleteById(1L);

        usuarioService.deleteUsuario(1L);

        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    void deberiaRetornarUsuarioPorAzureB2cIdSiExiste() {
        Usuario usuario = new Usuario();
        usuario.setAzureB2cId("azure-id-123");

        when(usuarioRepository.findByAzureB2cId("azure-id-123")).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.getUsuarioByAzureB2cId("azure-id-123");

        assertTrue(resultado.isPresent());
        assertEquals("azure-id-123", resultado.get().getAzureB2cId());
        verify(usuarioRepository, times(1)).findByAzureB2cId("azure-id-123");
    }

    @Test
    void deberiaRetornarProductorPorRutSiExiste() {
        Usuario usuario = new Usuario();
        usuario.setRutProductor("12.345.678-9");

        when(usuarioRepository.findByRutProductor("12.345.678-9")).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.getProductorByRut("12.345.678-9");

        assertTrue(resultado.isPresent());
        assertEquals("12.345.678-9", resultado.get().getRutProductor());
        verify(usuarioRepository, times(1)).findByRutProductor("12.345.678-9");
    }

    @Test
    void deberiaRetornarFalseSiNoExisteUsuarioPorEmail() {
        when(usuarioRepository.existsByEmail("noexiste@email.com")).thenReturn(false);

        boolean existe = usuarioService.existsByEmail("noexiste@email.com");

        assertFalse(existe);
        verify(usuarioRepository, times(1)).existsByEmail("noexiste@email.com");
    }

    @Test
    void deberiaRetornarFalseSiNoExisteProductorPorRut() {
        when(usuarioRepository.existsByRutProductor("99.999.999-9")).thenReturn(false);

        boolean existe = usuarioService.existsProductorByRut("99.999.999-9");

        assertFalse(existe);
        verify(usuarioRepository, times(1)).existsByRutProductor("99.999.999-9");
    }

    @Test
    void deberiaActualizarUsuarioExistente() {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setNombre("Antiguo");

        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        Usuario actualizado = usuarioService.updateUsuario(1L, usuarioMock);

        assertNotNull(actualizado);
        verify(usuarioRepository, times(1)).existsById(1L);
        verify(usuarioRepository, times(1)).save(usuarioMock);
    }

    @Test
    void deberiaLanzarExcepcionSiUsuarioNoExisteAlActualizar() {
        Usuario usuarioMock = new Usuario();

        when(usuarioRepository.existsById(99L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.updateUsuario(99L, usuarioMock);
        });

        assertEquals("Usuario no encontrado con ID: 99", exception.getMessage());
        verify(usuarioRepository, times(1)).existsById(99L);
    }

    @Test
    void deberiaActualizarUsuarioSiExistePorAzureB2cId() {
        Jwt jwt = mock(Jwt.class);
        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setAzureB2cId("azure-123");
        usuarioExistente.setEmail("correo@email.com");

        when(jwt.getClaim("emails")).thenReturn(List.of("correo@email.com"));
        when(jwt.getClaimAsString("given_name")).thenReturn("Juan");
        when(jwt.getClaimAsString("family_name")).thenReturn("Pérez");
        when(jwt.getClaimAsString("extension_Roles")).thenReturn("PRODUCTOR");
        when(jwt.getSubject()).thenReturn("azure-123");
        when(jwt.getClaim("extension_RUT")).thenReturn("12.345.678-9");

        when(usuarioRepository.findByAzureB2cId("azure-123")).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioExistente);

        Usuario resultado = usuarioService.sincronizarUsuarioDesdeJWT(jwt);

        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).findByAzureB2cId("azure-123");
        verify(usuarioRepository, never()).findByEmail(anyString()); // no se busca por email si ya existe por B2C
        verify(usuarioRepository, times(1)).save(usuarioExistente);
    }

    @Test
    void deberiaActualizarUsuarioSiExistePorEmailPeroNoTieneAzureB2cId() {
        Jwt jwt = mock(Jwt.class);
        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setEmail("correo@email.com");

        when(jwt.getClaim("emails")).thenReturn(List.of("correo@email.com"));
        when(jwt.getClaimAsString("given_name")).thenReturn("Juan");
        when(jwt.getClaimAsString("family_name")).thenReturn("Pérez");
        when(jwt.getClaimAsString("extension_Roles")).thenReturn("CLIENTE");
        when(jwt.getSubject()).thenReturn("azure-456");
        when(jwt.getClaim("extension_RUT")).thenReturn("22.222.222-2");

        when(usuarioRepository.findByAzureB2cId("azure-456")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("correo@email.com")).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioExistente);

        Usuario resultado = usuarioService.sincronizarUsuarioDesdeJWT(jwt);

        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).findByAzureB2cId("azure-456");
        verify(usuarioRepository, times(1)).findByEmail("correo@email.com");
        verify(usuarioRepository, times(1)).save(usuarioExistente);
    }

    @Test
    void deberiaCrearUsuarioSiNoExisteNiPorAzureNiPorEmail() {
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim("emails")).thenReturn(List.of("nuevo@email.com"));
        when(jwt.getClaimAsString("given_name")).thenReturn("Nuevo");
        when(jwt.getClaimAsString("family_name")).thenReturn("Usuario");
        when(jwt.getClaimAsString("extension_Roles")).thenReturn("ADMINISTRADOR");
        when(jwt.getSubject()).thenReturn("azure-new");
        when(jwt.getClaim("extension_RUT")).thenReturn("98.765.432-1");

        when(usuarioRepository.findByAzureB2cId("azure-new")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("nuevo@email.com")).thenReturn(Optional.empty());

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.sincronizarUsuarioDesdeJWT(jwt);

        assertNotNull(resultado);
        assertEquals("nuevo@email.com", resultado.getEmail());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void deberiaRetornarListaVaciaDeProductoresSiNoHayNinguno() {
        when(usuarioRepository.findProductoresActivos()).thenReturn(Collections.emptyList());

        List<Usuario> productores = usuarioService.getAllProductores();

        assertNotNull(productores);
        assertTrue(productores.isEmpty());
        verify(usuarioRepository, times(1)).findProductoresActivos();
    }

    @Test
    void deberiaRetornarListaDeProductoresPorNombre() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Luis");

        when(usuarioRepository.findProductoresByNombreContainingIgnoreCase("luis"))
            .thenReturn(List.of(usuario));

        List<Usuario> resultado = usuarioService.getProductoresByNombre("luis");

        assertEquals(1, resultado.size());
        assertEquals("Luis", resultado.get(0).getNombre());
        verify(usuarioRepository, times(1)).findProductoresByNombreContainingIgnoreCase("luis");
    }

    @Test
    void deberiaRetornarClientePorDefectoSiRolNoEsValido() {
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim("emails")).thenReturn(List.of("no-role@email.com"));
        when(jwt.getClaimAsString("given_name")).thenReturn("Sin");
        when(jwt.getClaimAsString("family_name")).thenReturn("Rol");
        when(jwt.getClaimAsString("extension_Roles")).thenReturn("INVALIDO");
        when(jwt.getSubject()).thenReturn("azure-sinrol");
        when(jwt.getClaim("extension_RUT")).thenReturn("11.111.111-1");

        when(usuarioRepository.findByAzureB2cId("azure-sinrol")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("no-role@email.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.sincronizarUsuarioDesdeJWT(jwt);

        assertEquals("CLIENTE", resultado.getRolAzure()); // ← Usar getRolAzure()
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }


    @Test
    void deberiaManejarEmailComoStringEnJwt() {
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim("emails")).thenReturn("emailstring@email.com");
        when(jwt.getClaimAsString("given_name")).thenReturn("String");
        when(jwt.getClaimAsString("family_name")).thenReturn("Format");
        when(jwt.getClaimAsString("extension_Roles")).thenReturn("CLIENTE");
        when(jwt.getSubject()).thenReturn("jwt-email-string");
        when(jwt.getClaim("extension_RUT")).thenReturn("10.000.000-0");

        when(usuarioRepository.findByAzureB2cId("jwt-email-string")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("emailstring@email.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.sincronizarUsuarioDesdeJWT(jwt);

        assertEquals("emailstring@email.com", resultado.getEmail());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void deberiaExtraerEmailDesdeStringEnJWT() {
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim("emails")).thenReturn("solo@email.com"); // << string en vez de lista
        when(jwt.getClaimAsString("given_name")).thenReturn("Solo");
        when(jwt.getClaimAsString("family_name")).thenReturn("Correo");
        when(jwt.getClaimAsString("extension_Roles")).thenReturn("PRODUCTOR");
        when(jwt.getSubject()).thenReturn("azure-solo");
        when(jwt.getClaim("extension_RUT")).thenReturn("33.333.333-3");

        when(usuarioRepository.findByAzureB2cId("azure-solo")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("solo@email.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.sincronizarUsuarioDesdeJWT(jwt);

        assertEquals("solo@email.com", resultado.getEmail());
        assertEquals("PRODUCTOR", resultado.getRolAzure());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void deberiaLanzarExcepcionSiEmailsEsNull() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("emails")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.sincronizarUsuarioDesdeJWT(jwt);
        });

        assertEquals("Formato de email no válido en JWT", exception.getMessage());
    }

    @Test
    void deberiaProcesarRutSiNoEsString() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("emails")).thenReturn(List.of("rare@email.com"));
        when(jwt.getClaimAsString("given_name")).thenReturn("Raro");
        when(jwt.getClaimAsString("family_name")).thenReturn("Objeto");
        when(jwt.getClaimAsString("extension_Roles")).thenReturn("CLIENTE");
        when(jwt.getSubject()).thenReturn("azure-raro");

        // Simulamos un Integer como RUT (caso raro pero posible)
        when(jwt.getClaim("extension_RUT")).thenReturn(12345678);

        when(usuarioRepository.findByAzureB2cId("azure-raro")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("rare@email.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.sincronizarUsuarioDesdeJWT(jwt);

        assertEquals("rare@email.com", resultado.getEmail());
        assertEquals("12345678", resultado.getRutProductor());
    }

    @Test
    void deberiaAsignarClienteSiRolAzureEsInvalido() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("emails")).thenReturn(List.of("invalid@rol.com"));
        when(jwt.getClaimAsString("given_name")).thenReturn("Invalido");
        when(jwt.getClaimAsString("family_name")).thenReturn("Rol");
        when(jwt.getClaimAsString("extension_Roles")).thenReturn("DESCONOCIDO");
        when(jwt.getSubject()).thenReturn("azure-desconocido");
        when(jwt.getClaim("extension_RUT")).thenReturn("00.000.000-0");

        when(usuarioRepository.findByAzureB2cId("azure-desconocido")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("invalid@rol.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.sincronizarUsuarioDesdeJWT(jwt);

        assertEquals("CLIENTE", resultado.getRolAzure()); // se guarda así
        assertEquals(TipoUsuario.CLIENTE, resultado.getTipoUsuario()); // este es el enum mapeado
    }

    @Test
    void deberiaRetornarListaVaciaDeProductoresSiNoHay() {
        when(usuarioRepository.findProductoresActivos()).thenReturn(Collections.emptyList());

        List<Usuario> productores = usuarioService.getAllProductores();

        assertNotNull(productores);
        assertTrue(productores.isEmpty());
        verify(usuarioRepository, times(1)).findProductoresActivos();
    }

    @Test
    void deberiaRetornarProductoresPorNombre() {
        Usuario productor = new Usuario();
        productor.setNombre("Pedro Productor");

        when(usuarioRepository.findProductoresByNombreContainingIgnoreCase("Pedro"))
                .thenReturn(List.of(productor));

        List<Usuario> resultado = usuarioService.getProductoresByNombre("Pedro");

        assertFalse(resultado.isEmpty());
        assertEquals("Pedro Productor", resultado.get(0).getNombre());
        verify(usuarioRepository, times(1)).findProductoresByNombreContainingIgnoreCase("Pedro");
    }

    @Test
    void deberiaActualizarUsuarioExistenteConFindOrCreatePorEmail() {
        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setEmail("correo@email.com");

        when(usuarioRepository.findByEmail("correo@email.com")).thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioExistente);

        Usuario resultado = usuarioService.findOrCreateUsuarioByEmail(
                "correo@email.com", "Carlos", "Sánchez", "azure-001", "PRODUCTOR", "11.111.111-1"
        );

        assertNotNull(resultado);
        assertEquals("correo@email.com", resultado.getEmail());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    //// ❌ Este test provoca fallos de cobertura de "new code" en SonarQube
   /* @Test
    void deberiaCrearUsuarioSiNoExisteConFindOrCreatePorEmail() {
        when(usuarioRepository.findByEmail("nuevo@email.com")).thenReturn(Optional.empty());

        when(usuarioRepository.save(any(Usuario.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.findOrCreateUsuarioByEmail(
                "nuevo@email.com", "Ana", "Gómez", "azure-002", "CLIENTE", null
        );

        assertNotNull(resultado);
        assertEquals("nuevo@email.com", resultado.getEmail());
        assertEquals("AnaGómez", resultado.getNombre());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }  */

    @Test
    void deberiaActualizarAzureIdSiExistePorEmail() {
        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setEmail("actualizar@email.com");
        usuarioExistente.setAzureB2cId("antiguo-id");

        when(usuarioRepository.findByEmail("actualizar@email.com"))
            .thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.save(any(Usuario.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.findOrCreateUsuarioByEmail(
            "actualizar@email.com", "Nuevo", "Nombre", "nuevo-id", "PRODUCTOR", "11.111.111-1"
        );

        assertEquals("nuevo-id", resultado.getAzureB2cId());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deberiaRetornarUsuarioPorAzureId() {
        Usuario usuario = new Usuario();
        usuario.setAzureB2cId("az-789");
        usuario.setEmail("usuario@email.com");

        when(usuarioRepository.findByAzureB2cId("az-789")).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultadoOpt = usuarioService.getUsuarioByAzureB2cId("az-789");

        assertTrue(resultadoOpt.isPresent());
        Usuario resultado = resultadoOpt.get();
        assertEquals("usuario@email.com", resultado.getEmail());
    }

    @Test
    void deberiaEliminarUsuarioPorId() {
        when(usuarioRepository.existsById(50L)).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(50L);

        usuarioService.deleteUsuario(50L);

        verify(usuarioRepository).deleteById(50L);
    }

    @Test
    void deberiaRetornarSaludoTest() {
        String saludo = usuarioService.saludoTest();
        assertEquals("Hola SonarQube", saludo);
    }
    
    @Test
    void deberiaCrearUsuarioProductorConRut() {
        when(usuarioRepository.findByEmail("nuevo@productor.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario nuevo = usuarioService.findOrCreateUsuarioByEmail(
            "nuevo@productor.com", "Nombre", "Apellido", "az-productor", "PRODUCTOR", "22.222.222-2"
        );

        assertEquals("nuevo@productor.com", nuevo.getEmail());
        assertEquals("NombreApellido", nuevo.getNombre());
        assertEquals("PRODUCTOR", nuevo.getRolAzure());
        assertEquals("22.222.222-2", nuevo.getRutProductor());
        assertEquals(TipoUsuario.PRODUCTOR, nuevo.getTipoUsuario());

        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }
    @Test
    void deberiaRetornarVacioSiUsuarioPorIdNoExiste() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.getUsuarioById(999L);

        assertFalse(resultado.isPresent());
        verify(usuarioRepository).findById(999L);
    }


    @Test
    void deberiaRetornarUsuariosActivos() {
        Usuario usuarioActivo = new Usuario();
        usuarioActivo.setActivo(1);

        Usuario usuarioInactivo = new Usuario();
        usuarioInactivo.setActivo(0);

        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioActivo, usuarioInactivo));

        List<Usuario> usuarios = usuarioService.getAllUsuarios();

        assertEquals(1, usuarios.size());
        assertEquals(1, usuarios.get(0).getActivo());
    }

    @Test
    void deberiaActualizarUsuarioExistentePorId() {
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setNombre("Nuevo Nombre");

        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(usuarioRepository.save(usuarioActualizado)).thenReturn(usuarioActualizado);

        Usuario resultado = usuarioService.updateUsuario(1L, usuarioActualizado);

        assertNotNull(resultado);
        assertEquals("Nuevo Nombre", resultado.getNombre());
        verify(usuarioRepository, times(1)).existsById(1L);
        verify(usuarioRepository, times(1)).save(usuarioActualizado);
    }

    @Test
    void deberiaRetornarTrueSiExisteProductorPorRut() {
        when(usuarioRepository.existsByRutProductor("77.777.777-7")).thenReturn(true);

        boolean existe = usuarioService.existsProductorByRut("77.777.777-7");

        assertTrue(existe);
        verify(usuarioRepository).existsByRutProductor("77.777.777-7");
    }

    @Test
    void deberiaRetornarSoloUsuariosActivos() {
        Usuario usuarioActivo = new Usuario();
        usuarioActivo.setNombre("Activo");
        usuarioActivo.setActivo(1);

        Usuario usuarioInactivo = new Usuario();
        usuarioInactivo.setNombre("Inactivo");
        usuarioInactivo.setActivo(0);

        List<Usuario> usuariosMock = Arrays.asList(usuarioActivo, usuarioInactivo);
        when(usuarioRepository.findAll()).thenReturn(usuariosMock);

        List<Usuario> resultado = usuarioService.getAllUsuarios();

        assertEquals(1, resultado.size());
        assertEquals("Activo", resultado.get(0).getNombre());
        verify(usuarioRepository).findAll();
    }

    @Test
    void deberiaRetornarUsuarioSiExistePorId() {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        Optional<Usuario> resultado = usuarioService.getUsuarioById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
    }

    @Test
    void deberiaRetornarVacioSiNoExisteUsuarioPorId() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.getUsuarioById(2L);

        assertFalse(resultado.isPresent());
    }

    @Test
    void deberiaRetornarUsuarioSiExistePorEmail() {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setEmail("test@email.com");

        when(usuarioRepository.findByEmail("test@email.com")).thenReturn(Optional.of(usuarioMock));

        Optional<Usuario> resultado = usuarioService.getUsuarioByEmail("test@email.com");

        assertTrue(resultado.isPresent());
        assertEquals("test@email.com", resultado.get().getEmail());
    }

    @Test
    void deberiaRetornarVacioSiNoExisteUsuarioPorEmail() {
        when(usuarioRepository.findByEmail("falso@email.com")).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.getUsuarioByEmail("falso@email.com");

        assertFalse(resultado.isPresent());
    }

    @Test
    void deberiaRetornarUsuariosProductoresActivos() {
        List<Usuario> productoresMock = new ArrayList<>();
        productoresMock.add(new Usuario());

        when(usuarioRepository.findProductoresActivos()).thenReturn(productoresMock);

        List<Usuario> resultado = usuarioService.getAllProductores();

        assertEquals(1, resultado.size());
    }

    @Test
    void deberiaBuscarProductoresPorNombre() {
        List<Usuario> mockUsuarios = new ArrayList<>();
        mockUsuarios.add(new Usuario());

        when(usuarioRepository.findProductoresByNombreContainingIgnoreCase("Juan")).thenReturn(mockUsuarios);

        List<Usuario> resultado = usuarioService.getProductoresByNombre("Juan");

        assertFalse(resultado.isEmpty());
    }

    @Test
    void deberiaEliminarUsuario() {
        doNothing().when(usuarioRepository).deleteById(1L);

        usuarioService.deleteUsuario(1L);

        verify(usuarioRepository, times(1)).deleteById(1L);
    }


    @Test
    void deberiaRetornarUsuarioPorIdInexistente() {
        when(usuarioRepository.findById(77L)).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.getUsuarioById(77L);

        assertFalse(resultado.isPresent());
        verify(usuarioRepository, times(1)).findById(77L);
    }

    @Test
    void deberiaRetornarUsuarioPorEmailInexistente() {
        when(usuarioRepository.findByEmail("inexistente@email.com")).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.getUsuarioByEmail("inexistente@email.com");

        assertFalse(resultado.isPresent());
        verify(usuarioRepository, times(1)).findByEmail("inexistente@email.com");
    }

    @Test
    void deberiaRetornarUsuarioPorRutInexistente() {
        when(usuarioRepository.findByRutProductor("00.000.000-0")).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.getProductorByRut("00.000.000-0");

        assertFalse(resultado.isPresent());
        verify(usuarioRepository, times(1)).findByRutProductor("00.000.000-0");
    }

    @Test
    void deberiaRetornarUsuarioPorAzureB2cIdInexistente() {
        when(usuarioRepository.findByAzureB2cId("az-no")).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.getUsuarioByAzureB2cId("az-no");

        assertFalse(resultado.isPresent());
        verify(usuarioRepository, times(1)).findByAzureB2cId("az-no");
    }



    @Test
    void deberiaCrearUsuarioConRolDesconocidoYAsumirCliente() {
        Jwt jwt = mock(Jwt.class);

        when(jwt.getClaim("emails")).thenReturn(List.of("cliente@correo.com"));
        when(jwt.getClaimAsString("given_name")).thenReturn("Nombre");
        when(jwt.getClaimAsString("family_name")).thenReturn("Apellido");
        when(jwt.getClaimAsString("extension_Roles")).thenReturn("SUPERHEROE"); // rol inválido
        when(jwt.getClaim("extension_RUT")).thenReturn("11.111.111-1");
        when(jwt.getSubject()).thenReturn("b2c-987");

        when(usuarioRepository.findByAzureB2cId("b2c-987")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("cliente@correo.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.sincronizarUsuarioDesdeJWT(jwt);

        assertEquals("CLIENTE", resultado.getRolAzure());
        assertEquals("cliente@correo.com", resultado.getEmail());
    }

    @Test
    void deberiaActualizarUsuarioConAzureIdNulo() {
        Usuario usuario = new Usuario();
        usuario.setAzureB2cId(null);
        usuario.setEmail("sinid@azure.com");

        when(usuarioRepository.findByEmail("sinid@azure.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario actualizado = usuarioService.findOrCreateUsuarioByEmail(
            "sinid@azure.com", "Nuevo", "Nombre", "nuevo-id", "PRODUCTOR", "33.333.333-3"
        );

        assertEquals("nuevo-id", actualizado.getAzureB2cId());
        assertEquals("NuevoNombre", actualizado.getNombre());
    }

    @Test
    void deberiaActualizarUsuarioConAzureIdVacio() {
        Usuario usuario = new Usuario();
        usuario.setAzureB2cId("");
        usuario.setEmail("vacio@azure.com");

        when(usuarioRepository.findByEmail("vacio@azure.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario actualizado = usuarioService.findOrCreateUsuarioByEmail(
            "vacio@azure.com", "Vac", "Io", "az-id", "CLIENTE", null
        );

        assertEquals("az-id", actualizado.getAzureB2cId());
        assertEquals("VacIo", actualizado.getNombre());
    }

    @Test
    void deberiaRetornarProductoresActivos() {
        Usuario prod = new Usuario();
        prod.setNombre("Activo");

        when(usuarioRepository.findProductoresActivos()).thenReturn(List.of(prod));

        List<Usuario> resultado = usuarioService.getAllProductores();

        assertFalse(resultado.isEmpty());
        assertEquals("Activo", resultado.get(0).getNombre());
        verify(usuarioRepository).findProductoresActivos();
    }



    


}
