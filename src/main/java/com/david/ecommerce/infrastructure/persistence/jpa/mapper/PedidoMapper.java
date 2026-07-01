package com.david.ecommerce.infrastructure.persistence.jpa.mapper;

import com.david.ecommerce.domain.pedido.DetallePedido;
import com.david.ecommerce.domain.pedido.Pedido;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.DetallePedidoEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.PedidoEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.UsuarioEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.ProductoEntity;

import java.util.List;
import java.util.stream.Collectors;

public class PedidoMapper {

    private PedidoMapper() {}

    public static PedidoEntity toEntity(Pedido domain, UsuarioEntity usuarioEntity,
                                         List<DetallePedidoEntity> detalleEntities) {
        if (domain == null) return null;
        PedidoEntity entity = new PedidoEntity();
        entity.setId(domain.getId());
        entity.setUsuario(usuarioEntity);
        entity.setDetalles(detalleEntities);
        entity.setFechaPedido(domain.getFechaPedido());
        entity.setTotal(domain.getTotal());
        entity.setEstado(domain.getEstado());
        return entity;
    }

    public static Pedido toDomain(PedidoEntity entity) {
        if (entity == null) return null;
        Pedido domain = new Pedido(entity.getUsuario().getId(), entity.getEstado());
        domain.setId(entity.getId());
        domain.setFechaPedido(entity.getFechaPedido());
        domain.setDetalles(entity.getDetalles().stream()
                .map(PedidoMapper::toDomainDetalle)
                .collect(Collectors.toList()));
        domain.calcularTotal();
        return domain;
    }

    public static DetallePedidoEntity toEntityDetalle(DetallePedido domain, PedidoEntity pedidoEntity,
                                                       ProductoEntity productoEntity) {
        if (domain == null) return null;
        DetallePedidoEntity entity = new DetallePedidoEntity();
        entity.setId(domain.getId());
        entity.setPedido(pedidoEntity);
        entity.setProducto(productoEntity);
        entity.setCantidad(domain.getCantidad());
        entity.setPrecioUnitario(domain.getPrecioUnitario());
        entity.setSubtotal(domain.getSubtotal());
        return entity;
    }

    public static DetallePedido toDomainDetalle(DetallePedidoEntity entity) {
        if (entity == null) return null;
        DetallePedido domain = new DetallePedido();
        domain.setId(entity.getId());
        domain.setProductoId(entity.getProducto().getId());
        domain.setProductoNombre(entity.getProducto().getNombre());
        domain.setCantidad(entity.getCantidad());
        domain.setPrecioUnitario(entity.getPrecioUnitario());
        domain.setSubtotal(entity.getSubtotal());
        return domain;
    }
}
