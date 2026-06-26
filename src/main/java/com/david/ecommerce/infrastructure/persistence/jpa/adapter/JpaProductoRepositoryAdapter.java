package com.david.ecommerce.infrastructure.persistence.jpa.adapter;

import com.david.ecommerce.domain.producto.Producto;
import com.david.ecommerce.domain.producto.ProductoRepository;
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
        var entity = ProductoMapper.toEntity(producto);
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
    public List<Producto> buscarConFiltros(String nombre, Producto.Categoria categoria,
                                           BigDecimal precioMin, BigDecimal precioMax, Integer stockMin) {
        com.david.ecommerce.infrastructure.persistence.jpa.entity.ProductoEntity.Categoria cat = null;
        if (categoria != null) {
            cat = com.david.ecommerce.infrastructure.persistence.jpa.entity.ProductoEntity.Categoria.valueOf(categoria.name());
        }
        return jpaRepository.buscarConFiltros(nombre, cat, precioMin, precioMax, stockMin)
                .stream()
                .map(ProductoMapper::toDomain)
                .collect(Collectors.toList());
    }
}
