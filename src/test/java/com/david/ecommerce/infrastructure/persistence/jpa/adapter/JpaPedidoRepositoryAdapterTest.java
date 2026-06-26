package com.david.ecommerce.infrastructure.persistence.jpa.adapter;

import com.david.ecommerce.domain.pedido.DetallePedido;
import com.david.ecommerce.domain.pedido.Pedido;
import com.david.ecommerce.infrastructure.persistence.jpa.AbstractIntegrationTest;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.ProductoEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.UsuarioEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Import(JpaPedidoRepositoryAdapter.class)
class JpaPedidoRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private JpaPedidoRepositoryAdapter adapter;

    @Autowired
    private TestEntityManager em;

    private Long usuarioId;
    private Long productoId;

    @BeforeEach
    void setUp() {
        var usuario = new UsuarioEntity();
        usuario.setNombre("Ana Martínez");
        usuario.setEmail("ana@ejemplo.com");
        usuario.setPassword("pass123");
        usuario.setRol(UsuarioEntity.Rol.CLIENTE);
        em.persistAndFlush(usuario);
        usuarioId = usuario.getId();

        var producto = new ProductoEntity();
        producto.setNombre("Producto Test");
        producto.setPrecio(new BigDecimal("100.00"));
        producto.setStock(20);
        producto.setCategoria(ProductoEntity.Categoria.OTROS);
        em.persistAndFlush(producto);
        productoId = producto.getId();

        em.clear();
    }

    @Test
    @DisplayName("findAll retorna todos los pedidos")
    void findAll_ConDatos_RetornaTodos() {
        crearPedidoPendiente();

        var resultados = adapter.findAll();

        assertThat(resultados).hasSize(1);
    }

    @Test
    @DisplayName("findById con ID existente retorna Optional con pedido")
    void findById_Existente_RetornaPedido() {
        var creado = crearPedidoPendiente();

        var resultado = adapter.findById(creado.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getUsuarioId()).isEqualTo(usuarioId);
        assertThat(resultado.get().getEstado()).isEqualTo(Pedido.EstadoPedido.PENDIENTE);
    }

    @Test
    @DisplayName("findById con ID inexistente retorna Optional vacío")
    void findById_NoExistente_RetornaVacio() {
        var resultado = adapter.findById(999L);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("save crea un nuevo pedido con detalles")
    void save_Crear_InsertaPedidoYDetalles() {
        var pedido = new Pedido();
        pedido.setUsuarioId(usuarioId);
        pedido.setTotal(new BigDecimal("300.00"));
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
        pedido.setFechaPedido(LocalDateTime.now());

        var detalle = new DetallePedido(productoId, "Producto Test", 3,
                new BigDecimal("100.00"), new BigDecimal("999.99"));
        pedido.agregarDetalle(detalle);

        var resultado = adapter.save(pedido);

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(resultado.getDetalles()).hasSize(1);
        assertThat(resultado.getDetalles().getFirst().getCantidad()).isEqualTo(3);
    }

    @Test
    @DisplayName("save actualiza un pedido existente")
    void save_Actualizar_RetornaPedidoActualizado() {
        var creado = crearPedidoPendiente();

        var pedido = adapter.findById(creado.getId()).orElseThrow();
        pedido.setEstado(Pedido.EstadoPedido.PAGADO);

        var resultado = adapter.save(pedido);

        assertThat(resultado.getEstado()).isEqualTo(Pedido.EstadoPedido.PAGADO);
    }

    @Test
    @DisplayName("deleteById elimina un pedido")
    void deleteById_Existente_PedidoEliminado() {
        var creado = crearPedidoPendiente();

        adapter.deleteById(creado.getId());

        var resultado = adapter.findById(creado.getId());
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByUsuarioId retorna pedidos para un usuario específico")
    void findByUsuarioId_ConPedidos_RetornaPedidosDelUsuario() {
        crearPedidoPendiente();

        var resultados = adapter.findByUsuarioId(usuarioId);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.getFirst().getUsuarioId()).isEqualTo(usuarioId);
    }

    @Test
    @DisplayName("findByUsuarioId sin pedidos retorna lista vacía")
    void findByUsuarioId_SinPedidos_RetornaVacio() {
        var resultados = adapter.findByUsuarioId(999L);

        assertThat(resultados).isEmpty();
    }

    @Test
    @DisplayName("@PrePersist sobreescribe subtotal en save con detalles (EC-4)")
    void save_ConDetalle_SubtotalSobreescritoPorPrePersist() {
        var pedido = new Pedido();
        pedido.setUsuarioId(usuarioId);
        pedido.setTotal(new BigDecimal("300.00"));
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
        pedido.setFechaPedido(LocalDateTime.now());

        var detalle = new DetallePedido(productoId, "Producto Test", 3,
                new BigDecimal("100.00"), new BigDecimal("999.99"));
        pedido.agregarDetalle(detalle);

        var resultado = adapter.save(pedido);

        assertThat(resultado.getDetalles()).hasSize(1);
        var detalleGuardado = resultado.getDetalles().getFirst();
        // @PrePersist recalcula: 100.00 * 3 = 300.00
        assertThat(detalleGuardado.getSubtotal())
                .isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(detalleGuardado.getSubtotal())
                .isNotEqualByComparingTo(new BigDecimal("999.99"));
    }

    private Pedido crearPedidoPendiente() {
        var pedido = new Pedido();
        pedido.setUsuarioId(usuarioId);
        pedido.setTotal(new BigDecimal("300.00"));
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
        pedido.setFechaPedido(LocalDateTime.now());

        var detalle = new DetallePedido(productoId, "Producto Test", 3,
                new BigDecimal("100.00"), new BigDecimal("300.00"));
        pedido.agregarDetalle(detalle);

        return adapter.save(pedido);
    }
}
