package com.david.ecommerce.producto.service;

import com.david.ecommerce.common.exception.RecursoNoEncontradoException;
import com.david.ecommerce.common.exception.ValidacionNegocioException;
import com.david.ecommerce.producto.dto.ProductoRequestDTO;
import com.david.ecommerce.producto.dto.ProductoResponseDTO;
import com.david.ecommerce.producto.model.Producto;
import com.david.ecommerce.producto.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository productoRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<ProductoResponseDTO> obtenerTodos() {
        List<Producto> productos = productoRepository.findAll();
        log.debug("📋 Listando {} productos", productos.size());
        return productos.stream()
                .map(ProductoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
        return new ProductoResponseDTO(producto);
    }

    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        Producto producto = new Producto();
        mapearDtoAEntidad(dto, producto);
        validarProducto(producto);

        Producto guardado = productoRepository.save(producto);

        log.info("🆕 Producto creado - ID: {}, Nombre: {}, Precio: ${}, Stock: {}",
                guardado.getId(), guardado.getNombre(), guardado.getPrecio(), guardado.getStock());

        return new ProductoResponseDTO(guardado);
    }

    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", id));
        mapearDtoAEntidad(dto, existente);
        validarProducto(existente);

        Producto actualizado = productoRepository.save(existente);

        log.info("✏️ Producto actualizado - ID: {}, Nombre: {}, Precio: ${}, Stock: {}",
                actualizado.getId(), actualizado.getNombre(), actualizado.getPrecio(), actualizado.getStock());

        return new ProductoResponseDTO(actualizado);
    }

    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Producto", id);
        }
        productoRepository.deleteById(id);
        log.warn("❌ Producto eliminado - ID: {}", id);
    }

    public List<ProductoResponseDTO> buscarConFiltros(String nombre,
                                                      Producto.Categoria categoria,
                                                      BigDecimal precioMin,
                                                      BigDecimal precioMax,
                                                      Integer stockMin) {
        List<Producto> productos = productoRepository.buscarConFiltros(
                nombre, categoria, precioMin, precioMax, stockMin);

        log.debug("🔍 Búsqueda con filtros - Nombre: {}, Categoría: {}, Resultados: {}",
                nombre, categoria, productos.size());

        return productos.stream()
                .map(ProductoResponseDTO::new)
                .collect(Collectors.toList());
    }

    private void mapearDtoAEntidad(ProductoRequestDTO dto, Producto producto) {
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setImagenUrl(dto.getImagenUrl());
        producto.setCategoria(dto.getCategoria());
    }

    private void validarProducto(Producto producto) {
        if (producto.getStock() < 0) {
            throw new ValidacionNegocioException("El stock no puede ser negativo");
        }
        if (producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidacionNegocioException("El precio no puede ser negativo");
        }
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new ValidacionNegocioException("El nombre del producto es obligatorio");
        }
    }
}