package com.partymap.backend.Service.Impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partymap.backend.Exceptions.NotFoundException;
import com.partymap.backend.Model.Productor;
import com.partymap.backend.Model.Usuario;
import com.partymap.backend.Repository.ProductorRepository;
import com.partymap.backend.Repository.UsuarioRepository;
import com.partymap.backend.Service.ProductorService;

/**
 * Implementación del servicio de productores.
 * Gestiona las operaciones CRUD de productores y validaciones de RUT.
 */
@Service
@Transactional
public class ProductorServiceImpl implements ProductorService {

    private final ProductorRepository productorRepository;
    private final UsuarioRepository usuarioRepository;

    public ProductorServiceImpl(ProductorRepository productorRepository, UsuarioRepository usuarioRepository) {
        this.productorRepository = productorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Obtiene todos los productores activos del sistema
     */
    @Override
    public List<Productor> getAllProductor() {
        return productorRepository.findAll().stream()
                .filter(productor -> productor.getActivo() == 1)
                .collect(Collectors.toList());
    }

    /**
     * Busca un productor por su ID
     */
    @Override
    public Optional<Productor> getProductorById(Long id) {
        return productorRepository.findById(id);
    }

    /**
     * Busca un productor por su RUT
     */
    @Override
    public Optional<Productor> getProductorByRut(String rut) {
        return productorRepository.findByRut(rut);
    }

    /**
     * Crea un nuevo productor con validaciones
     */
    @Override
    public Productor createProductor(Productor productor) throws IOException {
        // Validar que el productor tenga los datos requeridos
        if (productor.getNombreEmpresa() == null || productor.getRut() == null || 
            productor.getUsuario() == null) {
            throw new IllegalArgumentException("El productor debe tener nombre de empresa, RUT y usuario");
        }
        
        // Validar formato del RUT
        if (!validarRut(productor.getRut())) {
            throw new IllegalArgumentException("El formato del RUT no es válido");
        }
        
        // Verificar si ya existe un productor con el mismo RUT
        if (productorRepository.existsByRut(productor.getRut())) {
            throw new IllegalArgumentException("Ya existe un productor con el RUT: " + productor.getRut());
        }
        
        // Verificar si el usuario existe y es de tipo PRODUCTOR
        Usuario usuario = productor.getUsuario();
        if (usuario.getId() == null) {
            throw new IllegalArgumentException("El usuario debe tener un ID válido");
        }
        
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(usuario.getId());
        if (usuarioExistente.isEmpty()) {
            throw new IllegalArgumentException("El usuario especificado no existe");
        }
        
        if (!usuarioExistente.get().isProductor()) {
            throw new IllegalArgumentException("El usuario debe ser de tipo PRODUCTOR");
        }
        
        // Verificar si ya existe un productor asociado a este usuario
        if (productorRepository.existsByUsuarioId(usuario.getId())) {
            throw new IllegalArgumentException("Ya existe un productor asociado a este usuario");
        }
        
        // Guardar el productor
        return productorRepository.save(productor);
    }

    /**
     * Actualiza un productor existente
     */
    @Override
    public Productor updateProductor(Long id, Productor productor) {
        if (!productorRepository.existsById(id)) {
            throw new NotFoundException("Productor no encontrado con ID: " + id);
        }
        
        // Validar formato del RUT si se está actualizando
        if (productor.getRut() != null && !validarRut(productor.getRut())) {
            throw new IllegalArgumentException("El formato del RUT no es válido");
        }
        
        // Verificar si el nuevo RUT ya existe en otro productor
        if (productor.getRut() != null) {
            Optional<Productor> productorExistente = productorRepository.findByRut(productor.getRut());
            if (productorExistente.isPresent() && !productorExistente.get().getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otro productor con el RUT: " + productor.getRut());
            }
        }
        
        productor.setId(id);
        return productorRepository.save(productor);
    }

    /**
     * Elimina un productor del sistema (soft delete)
     */
    @Override
    public void deleteProductor(Productor productor) throws IOException {
        if (!productorRepository.existsById(productor.getId())) {
            throw new NotFoundException("Productor no encontrado con ID: " + productor.getId());
        }
        
        // Verificar si el productor tiene eventos asociados
        if (!productor.getEventos().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar un productor que tiene eventos asociados");
        }
        
        // Soft delete: cambiar estado activo a 0
        productor.setActivo(0);
        productorRepository.save(productor);
    }

    /**
     * Obtiene productores por nombre de empresa
     */
    @Override
    public List<Productor> getProductoresByNombreEmpresa(String nombreEmpresa) {
        return productorRepository.findByNombreEmpresaContainingIgnoreCase(nombreEmpresa);
    }

    /**
     * Verifica si existe un productor con el RUT especificado
     */
    @Override
    public boolean existsByRut(String rut) {
        return productorRepository.existsByRut(rut);
    }

    /**
     * Valida que el RUT tenga un formato válido
     */
    @Override
    public boolean validarRut(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return false;
        }
        
        // Validar formato básico: 7-8 dígitos + guión + dígito verificador
        if (!rut.matches("^[0-9]{7,8}-[0-9kK]$")) {
            return false;
        }
        
        String rutLimpio = rut.replaceAll("[.-]", "");
        if (rutLimpio.length() < 2) {
            return false;
        }
        
        String dv = rutLimpio.substring(rutLimpio.length() - 1);
        String numero = rutLimpio.substring(0, rutLimpio.length() - 1);
        
        try {
            int num = Integer.parseInt(numero);
            if (num < 1000000) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        
        return true;
    }
}
