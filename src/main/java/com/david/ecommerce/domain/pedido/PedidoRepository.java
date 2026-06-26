package com.david.ecommerce.domain.pedido;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository {

    List<Pedido> findAll();

    Optional<Pedido> findById(Long id);

    List<Pedido> findByUsuarioId(Long usuarioId);

    Pedido save(Pedido pedido);

    void deleteById(Long id);
}
