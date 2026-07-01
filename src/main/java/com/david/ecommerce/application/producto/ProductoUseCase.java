package com.david.ecommerce.application.producto;

import com.david.ecommerce.domain.common.Categoria;
import com.david.ecommerce.domain.producto.Producto;
import com.david.ecommerce.domain.producto.ProductoRepository;
import com.david.ecommerce.application.producto.dto.ProductoRequestDTO;
import com.david.ecommerce.application.producto.dto.ProductoResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProductoUseCase.class);

    private final ProductoRepository productoRepository;

    public ProductoUseCase(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<ProductoResponseDTO> obtenerTodos() {
        return productoRepository.findAll().stream()
                .map(ProductoResponseDTO::fromDomain)
                .collect(Collectors.toList());
    }

    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new com.david.ecommerce.common.exception.RecursoNoEncontradoException("Producto", id));
        return ProductoResponseDTO.fromDomain(producto);
    }

    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        Producto producto = new Producto(
                dto.getNombre(), dto.getDescripcion(),
                dto.getPrecio(), dto.getStock(),
                dto.getImagenUrl(), dto.getCategoria()
        );
        producto.validar();

        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado - ID: {}, Nombre: {}", guardado.getId(), guardado.getNombre());
        return ProductoResponseDTO.fromDomain(guardado);
    }

    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new com.david.ecommerce.common.exception.RecursoNoEncontradoException("Producto", id));

        existente.setNombre(dto.getNombre());
        existente.setDescripcion(dto.getDescripcion());
        existente.setPrecio(dto.getPrecio());
        existente.setStock(dto.getStock());
        existente.setImagenUrl(dto.getImagenUrl());
        existente.setCategoria(dto.getCategoria());
        existente.validar();

        Producto actualizado = productoRepository.save(existente);
        log.info("Producto actualizado - ID: {}", actualizado.getId());
        return ProductoResponseDTO.fromDomain(actualizado);
    }

    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new com.david.ecommerce.common.exception.RecursoNoEncontradoException("Producto", id);
        }
        productoRepository.deleteById(id);
        log.warn("Producto eliminado - ID: {}", id);
    }

    public List<ProductoResponseDTO> buscarConFiltros(String nombre, Categoria categoria,
                                                       BigDecimal precioMin, BigDecimal precioMax, Integer stockMin) {
        return productoRepository.buscarConFiltros(nombre, categoria, precioMin, precioMax, stockMin)
                .stream()
                .map(ProductoResponseDTO::fromDomain)
                .collect(Collectors.toList());
    }
}
