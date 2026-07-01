package com.david.ecommerce.infrastructure.persistence.jpa.adapter;

import com.david.ecommerce.domain.pedido.DetallePedido;
import com.david.ecommerce.domain.pedido.Pedido;
import com.david.ecommerce.domain.pedido.PedidoRepository;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.DetallePedidoEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.PedidoEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.UsuarioEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.mapper.PedidoMapper;
import com.david.ecommerce.infrastructure.persistence.jpa.repository.DetallePedidoJpaRepository;
import com.david.ecommerce.infrastructure.persistence.jpa.repository.PedidoJpaRepository;
import com.david.ecommerce.infrastructure.persistence.jpa.repository.UsuarioJpaRepository;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.ProductoEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.repository.ProductoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JpaPedidoRepositoryAdapter implements PedidoRepository {

    private final PedidoJpaRepository pedidoJpaRepository;
    private final DetallePedidoJpaRepository detallePedidoJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final ProductoJpaRepository productoJpaRepository;

    public JpaPedidoRepositoryAdapter(PedidoJpaRepository pedidoJpaRepository,
                                      DetallePedidoJpaRepository detallePedidoJpaRepository,
                                      UsuarioJpaRepository usuarioJpaRepository,
                                      ProductoJpaRepository productoJpaRepository) {
        this.pedidoJpaRepository = pedidoJpaRepository;
        this.detallePedidoJpaRepository = detallePedidoJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.productoJpaRepository = productoJpaRepository;
    }

    @Override
    public List<Pedido> findAll() {
        return pedidoJpaRepository.findAll().stream()
                .map(PedidoMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Pedido> findById(Long id) {
        return pedidoJpaRepository.findById(id)
                .map(PedidoMapper::toDomain);
    }

    @Override
    public List<Pedido> findByUsuarioId(Long usuarioId) {
        return pedidoJpaRepository.findByUsuarioId(usuarioId).stream()
                .map(PedidoMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Pedido save(Pedido pedido) {
        UsuarioEntity usuarioEntity = usuarioJpaRepository.findById(pedido.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + pedido.getUsuarioId()));

        PedidoEntity entity;
        if (pedido.getId() != null) {
            entity = pedidoJpaRepository.findById(pedido.getId())
                    .orElse(new PedidoEntity());
            entity.setId(pedido.getId());
        } else {
            entity = new PedidoEntity();
        }
        entity.setUsuario(usuarioEntity);
        entity.setFechaPedido(pedido.getFechaPedido());
        entity.setTotal(pedido.getTotal());
        entity.setEstado(pedido.getEstado());

        List<DetallePedidoEntity> detalleEntities = pedido.getDetalles().stream()
                .map(detalle -> {
                    ProductoEntity productoEntity = productoJpaRepository.findById(detalle.getProductoId())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalle.getProductoId()));
                    return PedidoMapper.toEntityDetalle(detalle, entity, productoEntity);
                })
                .collect(Collectors.toList());
        entity.setDetalles(detalleEntities);

        PedidoEntity saved = pedidoJpaRepository.save(entity);
        return PedidoMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        pedidoJpaRepository.deleteById(id);
    }
}
