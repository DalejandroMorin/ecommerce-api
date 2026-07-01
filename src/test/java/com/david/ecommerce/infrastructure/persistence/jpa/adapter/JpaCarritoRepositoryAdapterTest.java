package com.david.ecommerce.infrastructure.persistence.jpa.adapter;

import com.david.ecommerce.domain.carrito.Carrito;
import com.david.ecommerce.domain.carrito.ItemCarrito;
import com.david.ecommerce.domain.common.Categoria;
import com.david.ecommerce.domain.common.Rol;
import com.david.ecommerce.infrastructure.persistence.jpa.AbstractIntegrationTest;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.CarritoEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.ItemCarritoEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.ProductoEntity;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.UsuarioEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Import(JpaCarritoRepositoryAdapter.class)
class JpaCarritoRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private JpaCarritoRepositoryAdapter adapter;

    @Autowired
    private TestEntityManager em;

    private Long usuarioId;
    private Long productoId;

    @BeforeEach
    void setUp() {
        var usuario = new UsuarioEntity();
        usuario.setNombre("Carlos López");
        usuario.setEmail("carlos@ejemplo.com");
        usuario.setPassword("pass123");
        usuario.setRol(Rol.CLIENTE);
        em.persistAndFlush(usuario);
        usuarioId = usuario.getId();

        var producto = new ProductoEntity();
        producto.setNombre("Laptop Gamer");
        producto.setPrecio(new BigDecimal("2500.00"));
        producto.setStock(10);
        producto.setCategoria(Categoria.ELECTRONICA);
        em.persistAndFlush(producto);
        productoId = producto.getId();

        em.clear();
    }

    @Test
    @DisplayName("buscarPorUsuarioId con carrito existente retorna Optional con carrito")
    void buscarPorUsuarioId_Existente_RetornaCarrito() {
        var carrito = new Carrito();
        carrito.setUsuarioId(usuarioId);

        var guardado = adapter.guardar(carrito);

        var encontrado = adapter.buscarPorUsuarioId(usuarioId);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getId()).isEqualTo(guardado.getId());
        assertThat(encontrado.get().getUsuarioId()).isEqualTo(usuarioId);
    }

    @Test
    @DisplayName("buscarPorUsuarioId sin carrito retorna Optional vacío")
    void buscarPorUsuarioId_SinCarrito_RetornaVacio() {
        var resultado = adapter.buscarPorUsuarioId(999L);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("guardar con carrito nuevo (id null) crea carrito en BD")
    void guardar_Create_InsertaCarrito() {
        var carrito = new Carrito();
        carrito.setUsuarioId(usuarioId);

        var item = new ItemCarrito(productoId, "Laptop Gamer", 2, new BigDecimal("2500.00"));
        carrito.agregarItem(item);

        var resultado = adapter.guardar(carrito);

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(resultado.getItems()).hasSize(1);

        var encontrado = adapter.buscarPorUsuarioId(usuarioId);
        assertThat(encontrado).isPresent();
    }

    @Test
    @DisplayName("guardar con carrito existente (id presente) actualiza items via orphanRemoval")
    void guardar_Update_ReemplazaItems() {
        var carrito = new Carrito();
        carrito.setUsuarioId(usuarioId);

        var item1 = new ItemCarrito(productoId, "Laptop Gamer", 1, new BigDecimal("2500.00"));
        carrito.agregarItem(item1);

        var creado = adapter.guardar(carrito);

        var carritoActualizado = new Carrito();
        carritoActualizado.setId(creado.getId());
        carritoActualizado.setUsuarioId(usuarioId);

        var item2 = new ItemCarrito(productoId, "Laptop Gamer", 3, new BigDecimal("2500.00"));
        carritoActualizado.agregarItem(item2);

        var resultado = adapter.guardar(carritoActualizado);

        assertThat(resultado.getId()).isEqualTo(creado.getId());
        assertThat(resultado.getItems()).hasSize(1);
        assertThat(resultado.getItems().getFirst().getCantidad()).isEqualTo(3);
    }

    @Test
    @DisplayName("eliminarPorId elimina carrito con sus items en cascada")
    void eliminarPorId_Existente_EliminaCarrito() {
        var carrito = new Carrito();
        carrito.setUsuarioId(usuarioId);
        var guardado = adapter.guardar(carrito);

        adapter.eliminarPorId(guardado.getId());

        var encontrado = adapter.buscarPorUsuarioId(usuarioId);
        assertThat(encontrado).isEmpty();
    }

    @Test
    @DisplayName("buscarItemPorId retorna el item individual")
    void buscarItemPorId_Existente_RetornaItem() {
        var carrito = new Carrito();
        carrito.setUsuarioId(usuarioId);

        var item = new ItemCarrito(productoId, "Laptop Gamer", 2, new BigDecimal("2500.00"));
        carrito.agregarItem(item);

        var guardado = adapter.guardar(carrito);
        var itemId = guardado.getItems().getFirst().getId();

        var encontrado = adapter.buscarItemPorId(itemId);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getCantidad()).isEqualTo(2);
        assertThat(encontrado.get().getProductoId()).isEqualTo(productoId);
    }
}
