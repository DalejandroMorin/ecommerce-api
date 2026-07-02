package com.david.ecommerce.infrastructure.persistence.jpa.adapter;

import com.david.ecommerce.domain.common.Categoria;
import com.david.ecommerce.domain.producto.Producto;
import com.david.ecommerce.domain.producto.ProductoRepository;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.ProductoEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.mapper.ProductoMapper;
import com.david.ecommerce.infrastructure.persistence.jpa.repository.ProductoJpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JpaProductoRepositoryAdapter implements ProductoRepository {

    private final ProductoJpaRepository jpaRepository;

    public JpaProductoRepositoryAdapter(ProductoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Producto> findAll() {
        return jpaRepository.findAll().stream()
                .map(ProductoMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Producto> findById(Long id) {
        return jpaRepository.findById(id)
                .map(ProductoMapper::toDomain);
    }

    @Override
    public Producto save(Producto producto) {
        ProductoEntity entity;
        if (producto.getId() != null) {
            entity = jpaRepository.findById(producto.getId())
                    .orElseGet(() -> ProductoMapper.toEntity(producto));
            entity.setNombre(producto.getNombre());
            entity.setDescripcion(producto.getDescripcion());
            entity.setPrecio(producto.getPrecio());
            entity.setStock(producto.getStock());
            entity.setImagenUrl(producto.getImagenUrl());
            entity.setCategoria(producto.getCategoria());
        } else {
            entity = ProductoMapper.toEntity(producto);
        }
        var saved = jpaRepository.save(entity);
        return ProductoMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Producto> buscarConFiltros(String nombre, Categoria categoria,
                                           BigDecimal precioMin, BigDecimal precioMax, Integer stockMin) {
        return jpaRepository.buscarConFiltros(nombre, categoria, precioMin, precioMax, stockMin)
                .stream()
                .map(ProductoMapper::toDomain)
                .collect(Collectors.toList());
    }
}
