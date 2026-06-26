package com.david.ecommerce.infrastructure.persistence.jpa.mapper;

import com.david.ecommerce.domain.carrito.Carrito;
import com.david.ecommerce.domain.carrito.ItemCarrito;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.CarritoEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.ItemCarritoEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.UsuarioEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.ProductoEntity;

import java.util.stream.Collectors;

public class CarritoMapper {

    private CarritoMapper() {}

    public static CarritoEntity toEntity(Carrito domain, UsuarioEntity usuario,
                                          java.util.List<ItemCarritoEntity> itemEntities) {
        if (domain == null) return null;
        CarritoEntity entity = new CarritoEntity();
        entity.setId(domain.getId());
        entity.setUsuario(usuario);
        entity.setItems(itemEntities);
        return entity;
    }

    public static Carrito toDomain(CarritoEntity entity) {
        if (entity == null) return null;
        Carrito domain = new Carrito();
        domain.setId(entity.getId());
        domain.setUsuarioId(entity.getUsuario().getId());
        domain.setItems(entity.getItems().stream()
                .map(CarritoMapper::toDomainItem)
                .collect(Collectors.toList()));
        return domain;
    }

    public static ItemCarritoEntity toEntityItem(ItemCarrito domain, CarritoEntity carritoEntity,
                                                  ProductoEntity productoEntity) {
        if (domain == null) return null;
        ItemCarritoEntity entity = new ItemCarritoEntity();
        entity.setId(domain.getId());
        entity.setCarrito(carritoEntity);
        entity.setProducto(productoEntity);
        entity.setCantidad(domain.getCantidad());
        entity.setPrecioUnitario(domain.getPrecioUnitario());
        return entity;
    }

    public static ItemCarrito toDomainItem(ItemCarritoEntity entity) {
        if (entity == null) return null;
        ItemCarrito domain = new ItemCarrito();
        domain.setId(entity.getId());
        domain.setProductoId(entity.getProducto().getId());
        domain.setProductoNombre(entity.getProducto().getNombre());
        domain.setCantidad(entity.getCantidad());
        domain.setPrecioUnitario(entity.getPrecioUnitario());
        return domain;
    }
}
