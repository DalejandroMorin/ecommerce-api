package com.david.ecommerce.infrastructure.persistence.jpa.mapper;

import com.david.ecommerce.domain.common.Categoria;
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
        entity.setCategoria(domain.getCategoria());
        return entity;
    }

    public static Producto toDomain(ProductoEntity entity) {
        if (entity == null) return null;
        Producto domain = new Producto(
                entity.getNombre(), entity.getDescripcion(),
                entity.getPrecio(), entity.getStock(),
                entity.getImagenUrl(), entity.getCategoria()
        );
        domain.setId(entity.getId());
        domain.setFechaCreacion(entity.getFechaCreacion());
        domain.setFechaActualizacion(entity.getFechaActualizacion());
        return domain;
    }
}
