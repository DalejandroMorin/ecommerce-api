package com.david.ecommerce.infrastructure.persistence.jpa.entity;

import com.david.ecommerce.infrastructure.persistence.jpa.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DetallePedidoEntityTest extends AbstractIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("@PrePersist sobreescribe subtotal con precioUnitario × cantidad (EC-4)")
    void prePersist_SobreescribeSubtotal_CalculoCorrecto() {
        var usuario = new UsuarioEntity();
        usuario.setNombre("Test User");
        usuario.setEmail("test@ejemplo.com");
        usuario.setPassword("pass123");
        usuario.setRol(UsuarioEntity.Rol.CLIENTE);
        em.persistAndFlush(usuario);

        var producto = new ProductoEntity();
        producto.setNombre("Producto Test");
        producto.setPrecio(new BigDecimal("100.00"));
        producto.setStock(10);
        producto.setCategoria(ProductoEntity.Categoria.OTROS);
        em.persistAndFlush(producto);
        em.clear();

        var usuarioManaged = em.find(UsuarioEntity.class, usuario.getId());
        var productoManaged = em.find(ProductoEntity.class, producto.getId());

        var detalle = new DetallePedidoEntity();
        detalle.setProducto(productoManaged);
        detalle.setCantidad(3);
        detalle.setPrecioUnitario(new BigDecimal("150.50"));
        detalle.setSubtotal(new BigDecimal("999.99")); // valor incorrecto — debe ser sobreescrito

        var pedido = new PedidoEntity();
        pedido.setUsuario(usuarioManaged);
        pedido.setTotal(new BigDecimal("451.50"));
        pedido.setEstado(PedidoEntity.EstadoPedido.PENDIENTE);
        pedido.setDetalles(new java.util.ArrayList<>(java.util.List.of(detalle)));
        detalle.setPedido(pedido);

        var savedPedido = em.persistAndFlush(pedido);
        em.clear();

        var foundPedido = em.find(PedidoEntity.class, savedPedido.getId());
        var foundDetalle = foundPedido.getDetalles().getFirst();

        // @PrePersist recalcula: 150.50 * 3 = 451.50
        assertThat(foundDetalle.getSubtotal())
                .isEqualByComparingTo(new BigDecimal("451.50"));
        assertThat(foundDetalle.getSubtotal())
                .isNotEqualByComparingTo(new BigDecimal("999.99"));
    }
}
