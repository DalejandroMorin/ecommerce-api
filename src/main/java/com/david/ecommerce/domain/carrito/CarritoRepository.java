package com.david.ecommerce.domain.carrito;

import java.util.Optional;

public interface CarritoRepository {

    Optional<Carrito> buscarPorUsuarioId(Long usuarioId);

    Carrito guardar(Carrito carrito);

    void eliminarPorId(Long id);

    Optional<ItemCarrito> buscarItemPorId(Long itemId);

    void eliminarItem(ItemCarrito item);
}
