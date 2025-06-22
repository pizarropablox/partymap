package com.partymap.backend.Controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.partymap.backend.DTO.ProductorDTO;
import com.partymap.backend.DTO.ProductorResponseDTO;
import com.partymap.backend.DTO.UsuarioResponseDTO;
import com.partymap.backend.Exceptions.NotFoundException;
import com.partymap.backend.Model.Productor;
import com.partymap.backend.Model.Usuario;
import com.partymap.backend.Service.ProductorService;
import com.partymap.backend.Service.UsuarioService;

/**
 * Controlador REST para la gestión de productores.
 * Proporciona endpoints para operaciones CRUD de productores y funcionalidades adicionales.
 */
@RestController
@CrossOrigin
@RequestMapping("/productor")
public class ProductorController {

    private final ProductorService productorService;
    private final UsuarioService usuarioService;

    public ProductorController(ProductorService productorService, UsuarioService usuarioService) {
        this.productorService = productorService;
        this.usuarioService = usuarioService;
    }

    /**
     * Obtiene todos los productores del sistema
     * GET /productor
     */
    @GetMapping("/all")
    public ResponseEntity<List<ProductorResponseDTO>> getAllProductores() {
        List<Productor> productores = productorService.getAllProductor();
        List<ProductorResponseDTO> productoresDTO = productores.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productoresDTO);
    }

    /**
     * Obtiene un productor por su ID
     * GET /productor/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductorResponseDTO> getProductorById(@PathVariable Long id) {
        Optional<Productor> productor = productorService.getProductorById(id);
        if (productor.isPresent()) {
            return ResponseEntity.ok(convertToResponseDTO(productor.get()));
        } else {
            throw new NotFoundException("Productor no encontrado con ID: " + id);
        }
    }

    /**
     * Obtiene un productor por su RUT
     * GET /productor/rut/{rut}
     */
    @GetMapping("/rut/{rut}")
    public ResponseEntity<ProductorResponseDTO> getProductorByRut(@PathVariable String rut) {
        Optional<Productor> productor = productorService.getProductorByRut(rut);
        if (productor.isPresent()) {
            return ResponseEntity.ok(convertToResponseDTO(productor.get()));
        } else {
            throw new NotFoundException("Productor no encontrado con RUT: " + rut);
        }
    }

    /**
     * Crea un nuevo productor
     * POST /productor
     */
    @PostMapping
    public ResponseEntity<ProductorResponseDTO> createProductor(@RequestBody ProductorDTO productorDTO) {
        try {
            Productor productor = convertToEntity(productorDTO);
            Productor productorCreado = productorService.createProductor(productor);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(convertToResponseDTO(productorCreado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualiza un productor existente
     * PUT /productor/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductorResponseDTO> updateProductor(
            @PathVariable Long id,
            @RequestBody ProductorDTO productorDTO) {
        try {
            Productor productor = convertToEntity(productorDTO);
            Productor productorActualizado = productorService.updateProductor(id, productor);
            return ResponseEntity.ok(convertToResponseDTO(productorActualizado));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Elimina un productor (soft delete - establece activo = 0)
     * DELETE /productor/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductor(@PathVariable Long id) {
        try {
            Optional<Productor> productor = productorService.getProductorById(id);
            if (productor.isPresent()) {
                productorService.deleteProductor(productor.get());
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca productores por nombre de empresa
     * GET /productor/buscar?nombreEmpresa=Eventos
     * ejemplo : http://localhost:8083/productor/buscar?nombreEmpresa=Empresa%20de%20Marcelo
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductorResponseDTO>> buscarProductores(
            @RequestParam(required = false) String nombreEmpresa,
            @RequestParam(required = false) String rut) {
        
        List<Productor> productores = productorService.getAllProductor();
        
        // Aplicar filtros
        List<Productor> productoresFiltrados = productores.stream()
                .filter(productor -> nombreEmpresa == null || 
                    productor.getNombreEmpresa().toLowerCase().contains(nombreEmpresa.toLowerCase()))
                .filter(productor -> rut == null || 
                    productor.getRut().toLowerCase().contains(rut.toLowerCase()))
                .collect(Collectors.toList());
        
        List<ProductorResponseDTO> productoresDTO = productoresFiltrados.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productoresDTO);
    }

    /**
     * Obtiene productores por nombre de empresa específico
     * GET /productor/empresa/{nombreEmpresa}
     * ejemplo : http://localhost:8083/productor/empresa/Empresa%20de%20Marcelo
     */
    @GetMapping("/empresa/{nombreEmpresa}")
    public ResponseEntity<List<ProductorResponseDTO>> getProductoresByNombreEmpresa(@PathVariable String nombreEmpresa) {
        List<Productor> productores = productorService.getProductoresByNombreEmpresa(nombreEmpresa);
        List<ProductorResponseDTO> productoresDTO = productores.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productoresDTO);
    }

    /**
     * Valida si un RUT es válido
     * GET /productor/validar-rut?rut=12345678-9
     */
    @GetMapping("/validar-rut")
    public ResponseEntity<Boolean> validarRut(@RequestParam String rut) {
        boolean rutValido = productorService.validarRut(rut);
        return ResponseEntity.ok(rutValido);
    }

    /**
     * Verifica si existe un productor con el RUT especificado
     * GET /productor/existe-rut?rut=12345678-9
     */
    @GetMapping("/existe-rut")
    public ResponseEntity<Boolean> existeRut(@RequestParam String rut) {
        boolean existe = productorService.existsByRut(rut);
        return ResponseEntity.ok(existe);
    }

    // Métodos de conversión privados

    /**
     * Convierte un Productor a ProductorResponseDTO
     */
    private ProductorResponseDTO convertToResponseDTO(Productor productor) {
        ProductorResponseDTO dto = new ProductorResponseDTO();
        dto.setId(productor.getId());
        dto.setNombreEmpresa(productor.getNombreEmpresa());
        dto.setRut(productor.getRut());
        dto.setActivo(productor.getActivo());
        dto.setFechaCreacion(productor.getFechaCreacion());
        dto.setFechaModificacion(productor.getFechaModificacion());
        
        // Convertir usuario si existe
        if (productor.getUsuario() != null) {
            dto.setUsuario(convertUsuarioToResponseDTO(productor.getUsuario()));
        }
        
        return dto;
    }

    /**
     * Convierte un ProductorDTO a Productor
     */
    private Productor convertToEntity(ProductorDTO dto) {
        Productor productor = new Productor();
        productor.setId(dto.getId());
        productor.setNombreEmpresa(dto.getNombreEmpresa());
        productor.setRut(dto.getRut());
        
        // Buscar y asignar usuario por ID
        if (dto.getUsuarioId() != null) {
            Optional<Usuario> usuario = usuarioService.getUsuarioById(dto.getUsuarioId());
            if (usuario.isPresent()) {
                productor.setUsuario(usuario.get());
            } else {
                throw new IllegalArgumentException("Usuario no encontrado con ID: " + dto.getUsuarioId());
            }
        }
        
        return productor;
    }

    /**
     * Convierte un Usuario a UsuarioResponseDTO
     */
    private UsuarioResponseDTO convertUsuarioToResponseDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setTipoUsuario(usuario.getTipoUsuario());
        dto.setActivo(usuario.getActivo());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        dto.setFechaModificacion(usuario.getFechaModificacion());
        return dto;
    }
} 