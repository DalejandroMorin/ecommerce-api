package com.david.ecommerce.infrastructure.persistence.jpa.entity;

import com.david.ecommerce.domain.common.Categoria;
import com.david.ecommerce.domain.common.EstadoPedido;
import com.david.ecommerce.domain.common.Rol;
import com.david.ecommerce.infrastructure.persistence.jpa.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PedidoEntityTest extends AbstractIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("Cascade ALL persiste PedidoEntity con DetallePedidoEntity hijos")
    void cascadePersist_ConDetalles_GuardaTodoCorrectamente() {
        var usuario = crearUsuario();
        em.persistAndFlush(usuario);

        var producto = crearProducto();
        em.persistAndFlush(producto);
        em.clear();

        var usuarioManaged = em.find(UsuarioEntity.class, usuario.getId());
        var productoManaged = em.find(ProductoEntity.class, producto.getId());

        var detalle = new DetallePedidoEntity();
        detalle.setProducto(productoManaged);
        detalle.setCantidad(3);
        detalle.setPrecioUnitario(new BigDecimal("100.00"));
        detalle.setSubtotal(new BigDecimal("999.99")); // valor incorrecto que @PrePersist sobreescribe

        var pedido = new PedidoEntity();
        pedido.setUsuario(usuarioManaged);
        pedido.setTotal(new BigDecimal("300.00"));
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDetalles(new java.util.ArrayList<>(java.util.List.of(detalle)));
        detalle.setPedido(pedido);

        var saved = em.persistAndFlush(pedido);
        em.clear();

        var found = em.find(PedidoEntity.class, saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getUsuario().getId()).isEqualTo(usuario.getId());
        assertThat(found.getDetalles()).hasSize(1);
        assertThat(found.getDetalles().getFirst().getProducto().getId()).isEqualTo(producto.getId());
        assertThat(found.getDetalles().getFirst().getCantidad()).isEqualTo(3);
        assertThat(found.getDetalles().getFirst().getPrecioUnitario())
                .isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("@PrePersist establece fechaPedido y estado por defecto PENDIENTE")
    void prePersist_EstableceFechaYEstadoPorDefecto() {
        var usuario = crearUsuario();
        em.persistAndFlush(usuario);

        var pedido = new PedidoEntity();
        pedido.setUsuario(usuario);
        pedido.setTotal(new BigDecimal("500.00"));
        // estado queda null → @PrePersist debe asignar PENDIENTE

        var now = LocalDateTime.now();
        var beforePersist = now.withNano((now.getNano() / 1000) * 1000);
        var saved = em.persistAndFlush(pedido);
        em.clear();

        var found = em.find(PedidoEntity.class, saved.getId());

        assertThat(found.getFechaPedido()).isNotNull();
        assertThat(found.getFechaPedido()).isAfterOrEqualTo(beforePersist);
        assertThat(found.getFechaPedido()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(found.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
    }

    @Test
    @DisplayName("Verificar que todos los EstadoPedido del dominio existen")
    void enumSync_TodosLosEstadosMapean() {
        for (EstadoPedido domainEstado : EstadoPedido.values()) {
            assertThat(domainEstado.name()).isEqualTo(domainEstado.name());
        }
    }

    private UsuarioEntity crearUsuario() {
        var usuario = new UsuarioEntity();
        usuario.setNombre("Ana Martínez");
        usuario.setEmail("ana@ejemplo.com");
        usuario.setPassword("pass123");
        usuario.setRol(Rol.CLIENTE);
        return usuario;
    }

    private ProductoEntity crearProducto() {
        var producto = new ProductoEntity();
        producto.setNombre("Producto Pedido");
        producto.setPrecio(new BigDecimal("100.00"));
        producto.setStock(20);
        producto.setCategoria(Categoria.OTROS);
        return producto;
    }
}
