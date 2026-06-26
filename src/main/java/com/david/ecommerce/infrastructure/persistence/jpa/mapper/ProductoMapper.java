package com.david.ecommerce.infrastructure.persistence.jpa.mapper;

import com.david.ecommerce.domain.producto.Producto;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.ProductoEntity;

public class ProductoMapper {

    private ProductoMapper() {}

    public static ProductoEntity toEntity(Producto domain) {
        if (domain == null) return null;
        ProductoEntity entity = new ProductoEntity();
        entity.setId(domain.getId());
        entity.setNombre(domain.getNombre());
        entity.setDescripcion(domain.getDescripcion());
        entity.setPrecio(domain.getPrecio());
        entity.setStock(domain.getStock());
        entity.setImagenUrl(domain.getImagenUrl());
        if (domain.getCategoria() != null) {
            entity.setCategoria(ProductoEntity.Categoria.valueOf(domain.getCategoria().name()));
        }
        return entity;
    }

    public static Producto toDomain(ProductoEntity entity) {
        if (entity == null) return null;
        Producto domain = new Producto();
        domain.setId(entity.getId());
        domain.setNombre(entity.getNombre());
        domain.setDescripcion(entity.getDescripcion());
        domain.setPrecio(entity.getPrecio());
        domain.setStock(entity.getStock());
        domain.setImagenUrl(entity.getImagenUrl());
        if (entity.getCategoria() != null) {
            domain.setCategoria(Producto.Categoria.valueOf(entity.getCategoria().name()));
        }
        domain.setFechaCreacion(entity.getFechaCreacion());
        domain.setFechaActualizacion(entity.getFechaActualizacion());
        return domain;
    }
}
