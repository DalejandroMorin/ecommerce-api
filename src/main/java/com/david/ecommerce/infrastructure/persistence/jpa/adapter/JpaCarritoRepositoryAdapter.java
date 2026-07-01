package com.david.ecommerce.infrastructure.persistence.jpa.adapter;

import com.david.ecommerce.domain.carrito.Carrito;
import com.david.ecommerce.domain.carrito.CarritoRepository;
import com.david.ecommerce.domain.carrito.ItemCarrito;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.CarritoEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.ItemCarritoEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.UsuarioEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.mapper.CarritoMapper;
import com.david.ecommerce.infrastructure.persistence.jpa.repository.CarritoJpaRepository;
import com.david.ecommerce.infrastructure.persistence.jpa.repository.ItemCarritoJpaRepository;
import com.david.ecommerce.infrastructure.persistence.jpa.repository.UsuarioJpaRepository;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.ProductoEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.repository.ProductoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JpaCarritoRepositoryAdapter implements CarritoRepository {

    private final CarritoJpaRepository carritoJpaRepository;
    private final ItemCarritoJpaRepository itemCarritoJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final ProductoJpaRepository productoJpaRepository;

    public JpaCarritoRepositoryAdapter(CarritoJpaRepository carritoJpaRepository,
                                       ItemCarritoJpaRepository itemCarritoJpaRepository,
                                       UsuarioJpaRepository usuarioJpaRepository,
                                       ProductoJpaRepository productoJpaRepository) {
        this.carritoJpaRepository = carritoJpaRepository;
        this.itemCarritoJpaRepository = itemCarritoJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.productoJpaRepository = productoJpaRepository;
    }

    @Override
    public Optional<Carrito> buscarPorUsuarioId(Long usuarioId) {
        return carritoJpaRepository.findByUsuarioId(usuarioId)
                .map(CarritoMapper::toDomain);
    }

    @Override
    public Carrito guardar(Carrito carrito) {
        UsuarioEntity usuario = usuarioJpaRepository.findById(carrito.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + carrito.getUsuarioId()));

        CarritoEntity entity;
        if (carrito.getId() != null) {
            entity = carritoJpaRepository.findById(carrito.getId())
                    .orElse(new CarritoEntity());
            entity.setId(carrito.getId());
        } else {
            entity = new CarritoEntity();
        }
        entity.setUsuario(usuario);

        List<ItemCarritoEntity> itemEntities = carrito.getItems().stream()
                .map(item -> {
                    ProductoEntity producto = productoJpaRepository.findById(item.getProductoId())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));
                    ItemCarritoEntity itemEntity = CarritoMapper.toEntityItem(item, entity, producto);
                    return itemEntity;
                })
                .collect(Collectors.toList());
        entity.setItems(itemEntities);

        CarritoEntity saved = carritoJpaRepository.save(entity);
        return CarritoMapper.toDomain(saved);
    }

    @Override
    public void eliminarPorId(Long id) {
        carritoJpaRepository.deleteById(id);
    }

    @Override
    public Optional<ItemCarrito> buscarItemPorId(Long itemId) {
        return itemCarritoJpaRepository.findById(itemId)
                .map(CarritoMapper::toDomainItem);
    }

    @Override
    public void eliminarItem(ItemCarrito item) {
        itemCarritoJpaRepository.deleteById(item.getId());
    }
}
